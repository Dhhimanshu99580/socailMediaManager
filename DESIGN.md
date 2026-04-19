# Social Media Manager — Design Document

---

## High Level Design (HLD)

```
                        ┌─────────────────────┐
                        │   React Frontend     │
                        │  (Content Calendar,  │
                        │   Post Composer,     │
                        │   Analytics UI)      │
                        └────────┬────────────┘
                                 │ HTTPS
                        ┌────────▼────────────┐
                        │   API Gateway /      │
                        │   Load Balancer      │
                        │   (AWS ALB)          │
                        └────────┬────────────┘
                                 │
               ┌─────────────────▼──────────────────┐
               │        Spring Boot Application      │
               │                                     │
               │  ┌──────────┐  ┌─────────────────┐ │
               │  │   Auth   │  │  Post Management │ │
               │  │ (JWT +   │  │  (Schedule,      │ │
               │  │  OAuth2) │  │   Publish,       │ │
               │  └──────────┘  │   Analytics)     │ │
               │                └─────────────────┘ │
               └───┬──────────────┬─────────────────┘
                   │              │
        ┌──────────▼──┐    ┌──────▼──────────┐
        │  PostgreSQL  │    │   Redis          │
        │  (AWS RDS)   │    │  (OAuth state,  │
        │              │    │   caching,      │
        │  - users     │    │   rate limiting)│
        │  - tokens    │    └─────────────────┘
        │  - posts     │
        │  - analytics │          │
        └─────────────┘    ┌──────▼──────────┐
                           │     Kafka        │
                           │ (scheduled post  │
                           │  delivery queue) │
                           └──────┬──────────┘
                                  │
               ┌──────────────────▼──────────────────┐
               │         Platform Router              │
               │                                     │
               │  ┌──────────┐  ┌──────────────────┐ │
               │  │ Twitter  │  │    Instagram     │ │
               │  │ Service  │  │    Service       │ │
               │  └──────────┘  └──────────────────┘ │
               │  ┌──────────┐  ┌──────────────────┐ │
               │  │ Facebook │  │    YouTube       │ │
               │  │ Service  │  │    Service       │ │
               │  └──────────┘  └──────────────────┘ │
               └──────────────────────────────────────┘
                         │              │
              ┌──────────▼──┐   ┌───────▼──────┐
              │  Twitter API │   │ Meta Graph   │
              │  api.x.com   │   │ API          │
              └─────────────┘   └──────────────┘
```

---

## What to build — ordered by priority

### Phase 1 — Core complete (2–3 months) ← YOU ARE HERE
- [x] User registration + login (JWT)
- [x] Twitter OAuth2 PKCE flow
- [x] Post to Twitter (endpoint wired)
- [ ] Token refresh (Twitter tokens expire in 2hrs)
- [ ] TwitterPostResponse (currently empty class)
- [ ] Global exception handler (@RestControllerAdvice)
- [ ] Scheduled post pipeline (save → cron → publish)
- [ ] Move OAuthStateStore from in-memory to Redis or DB

### Phase 2 — Multi-platform (3–4 months)
- [ ] Refactor to SocialMediaService interface (platform abstraction)
- [ ] Instagram integration (Meta Graph API)
- [ ] Facebook Pages integration
- [ ] YouTube integration
- [ ] Platform-aware token storage (one user, multiple platform tokens)

### Phase 3 — SaaS features (3–4 months)
- [ ] Workspace / organisation model (agencies managing multiple clients)
- [ ] Team roles (ADMIN, EDITOR, VIEWER)
- [ ] React frontend — content calendar, post composer
- [ ] Stripe subscription billing
- [ ] Analytics dashboard (pull engagement data per platform)
- [ ] AI caption generator (Claude/OpenAI API)
- [ ] Media library (S3 for image/video storage)

### Phase 4 — Production ready (2–3 months)
- [ ] Replace @Scheduled cron with Kafka consumer
- [ ] Move to AWS (RDS + ECS/EC2 + ALB)
- [ ] Encrypt stored OAuth tokens at rest
- [ ] Rate limiting per user (bucket4j or Redis)
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Monitoring (AWS CloudWatch or Grafana)

---

## Low Level Design (LLD)

---

### Database Schema

#### `user_registration`
| Column       | Type         | Constraints                  |
|-------------|--------------|------------------------------|
| id          | BIGINT       | PK, AUTO_INCREMENT           |
| name        | VARCHAR(30)  | NOT NULL                     |
| username    | VARCHAR(30)  | NOT NULL, UNIQUE             |
| password    | VARCHAR(255) | NOT NULL (BCrypt hash)       |
| email       | VARCHAR(100) | NOT NULL, UNIQUE             |
| mobilenumber| VARCHAR(10)  | NOT NULL, UNIQUE             |
| age         | INT          | NOT NULL, CHECK (age >= 13)  |
| country     | VARCHAR(50)  | NOT NULL                     |
| state       | VARCHAR(50)  | NULLABLE                     |
| created_at  | TIMESTAMP    | DEFAULT NOW()                |
| updated_at  | TIMESTAMP    | ON UPDATE NOW()              |

---

#### `user_tokens` (Twitter OAuth tokens per user)
| Column        | Type          | Constraints          |
|--------------|---------------|----------------------|
| id           | BIGINT        | PK, AUTO_INCREMENT   |
| username     | VARCHAR(30)   | NOT NULL, UNIQUE, FK → user_registration.username |
| platform     | VARCHAR(20)   | NOT NULL (TWITTER, INSTAGRAM etc.) |
| token        | VARCHAR(2048) | NOT NULL             |
| refresh_token| VARCHAR(2048) | NULLABLE             |
| expires_at   | TIMESTAMP     | NULLABLE             |
| added_on     | TIMESTAMP     | DEFAULT NOW()        |
| updated_on   | TIMESTAMP     | ON UPDATE NOW()      |

> NOTE: Add (username, platform) as composite unique key when multi-platform is added.
> NOTE: Encrypt token + refresh_token at rest in production.

---

#### `post_details` (scheduled posts queue)
| Column        | Type          | Constraints                              |
|--------------|---------------|------------------------------------------|
| id           | BIGINT        | PK, AUTO_INCREMENT                       |
| username     | VARCHAR(30)   | NOT NULL, FK → user_registration.username|
| platform     | VARCHAR(20)   | NOT NULL (TWITTER, INSTAGRAM etc.)       |
| data         | VARCHAR(2048) | NOT NULL (post content)                  |
| media_url    | VARCHAR(500)  | NULLABLE (S3 URL for images/videos)      |
| eligible_time| TIMESTAMP     | NOT NULL (when to publish)               |
| status       | VARCHAR(20)   | NOT NULL DEFAULT 'PENDING' (PENDING, PUBLISHED, FAILED) |
| retry_count  | INT           | DEFAULT 0                                |
| error_message| VARCHAR(500)  | NULLABLE (last failure reason)           |
| added_on     | TIMESTAMP     | DEFAULT NOW()                            |

---

#### `oauth_state` (replaces in-memory OAuthStateStore — needed for prod)
| Column        | Type          | Constraints          |
|--------------|---------------|----------------------|
| id           | BIGINT        | PK, AUTO_INCREMENT   |
| state        | VARCHAR(100)  | NOT NULL, UNIQUE     |
| code_verifier| VARCHAR(200)  | NOT NULL             |
| username     | VARCHAR(30)   | NOT NULL             |
| created_at   | TIMESTAMP     | DEFAULT NOW()        |
| expires_at   | TIMESTAMP     | NOT NULL (state valid for 10 mins) |

---

#### `workspace` (future — Phase 3, for agency use)
| Column      | Type         | Constraints        |
|------------|--------------|---------------------|
| id         | BIGINT       | PK                  |
| name       | VARCHAR(100) | NOT NULL            |
| owner      | VARCHAR(30)  | FK → user_registration.username |
| plan       | VARCHAR(20)  | FREE / CREATOR / BUSINESS / AGENCY |
| created_at | TIMESTAMP    | DEFAULT NOW()       |

---

#### `post_analytics` (future — Phase 3)
| Column       | Type      | Constraints                    |
|-------------|-----------|-------------------------------|
| id          | BIGINT    | PK                             |
| post_id     | BIGINT    | FK → post_details.id           |
| platform    | VARCHAR(20)| NOT NULL                      |
| likes       | INT       | DEFAULT 0                      |
| reposts     | INT       | DEFAULT 0                      |
| impressions | INT       | DEFAULT 0                      |
| fetched_at  | TIMESTAMP | DEFAULT NOW()                  |

---

### API Endpoints

#### Auth
| Method | Path                | Auth    | Request Body                                      | Response                          |
|--------|---------------------|---------|---------------------------------------------------|-----------------------------------|
| POST   | /user-registration  | None    | `{name, username, password, email, mobilenumber, country, age, state}` | `{name, email, mobileno}` |
| POST   | /user-login         | None    | `{username, password}`                            | `{token, username}`               |

---

#### Twitter OAuth
| Method | Path                    | Auth  | Params              | Response                  |
|--------|-------------------------|-------|---------------------|---------------------------|
| GET    | /api/twitter/authorize  | JWT   | -                   | Twitter auth URL (string) |
| GET    | /api/twitter/callback   | None  | `?code=&state=`     | "Twitter connected"       |

---

#### Post Management
| Method | Path                    | Auth | Request Body                               | Response         |
|--------|-------------------------|------|--------------------------------------------|------------------|
| POST   | /mediaManager/v1/post   | JWT  | `{data, timeToPost, service}`              | `{id, text}`     |
| GET    | /mediaManager/v1/posts  | JWT  | -                                          | List of posts    |
| DELETE | /mediaManager/v1/post/{id} | JWT | -                                        | 204 No Content   |

---

#### Analytics (Phase 3)
| Method | Path                         | Auth | Response                              |
|--------|------------------------------|------|---------------------------------------|
| GET    | /analytics/summary           | JWT  | Totals across all platforms           |
| GET    | /analytics/{platform}        | JWT  | Per-platform breakdown                |
| GET    | /analytics/post/{id}         | JWT  | Stats for a single post               |

---

#### Workspace / Team (Phase 3)
| Method | Path                         | Auth  | Request Body              | Response          |
|--------|------------------------------|-------|---------------------------|-------------------|
| POST   | /workspace                   | JWT   | `{name}`                  | Workspace object  |
| POST   | /workspace/{id}/invite       | JWT   | `{email, role}`           | 200 OK            |
| GET    | /workspace/{id}/members      | JWT   | -                         | List of members   |

---

### Kafka Topics (Phase 4)

| Topic                  | Producer              | Consumer              | Purpose                          |
|------------------------|----------------------|----------------------|----------------------------------|
| `posts.scheduled`      | Post Controller      | Post Delivery Service | Trigger post at eligible_time   |
| `posts.published`      | Post Delivery Service| Analytics Service     | Record successful publish        |
| `posts.failed`         | Post Delivery Service| Retry / Alert Service | Handle failures, trigger retry   |
| `tokens.expired`       | Token Monitor        | Token Refresh Service | Refresh OAuth tokens proactively |

---

### AWS Infrastructure (Phase 4)

```
Route 53 (DNS)
    → ALB (Load Balancer)
        → ECS Fargate (Spring Boot containers, auto-scaled)
            → RDS PostgreSQL (Multi-AZ for prod)
            → ElastiCache Redis (OAuth state, rate limiting, caching)
            → MSK (Managed Kafka)
            → S3 (media storage)
            → Secrets Manager (DB password, JWT secret, API keys)
```

**Cost estimate (small scale, ~100 users):**
- RDS db.t3.micro: ~$15/month
- ECS Fargate (1 task): ~$10/month
- ElastiCache t3.micro: ~$15/month
- S3 + misc: ~$5/month
- **Total: ~$45/month** (covered by 4–5 paying Creator tier customers)

---

## Environment Strategy

| Environment | Database             | Kafka   | Redis    | Notes                    |
|-------------|---------------------|---------|----------|--------------------------|
| local       | PostgreSQL (local)  | None    | None     | In-memory state store OK |
| staging     | RDS PostgreSQL      | None    | Redis    | Mirror of prod, no Kafka |
| production  | RDS PostgreSQL      | MSK     | Redis    | Full stack                |
