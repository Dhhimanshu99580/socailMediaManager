Social Media Manager
A Spring Boot REST API that lets users register, authenticate, connect their Twitter/X account via OAuth 2.0 (PKCE), and post tweets programmatically.

Table of Contents
Tech Stack
Project Structure
Getting Started
Configuration
API Reference
Authentication Flow
Twitter OAuth 2.0 Flow
Database Schema
Error Handling
Running Tests
Contributing
Tech Stack
Layer	Technology
Language	Java 17
Framework	Spring Boot 3.3
Build	Maven
Database	PostgreSQL
ORM	Spring Data JPA / Hibernate 6
Security	Spring Security 6, JWT (JJWT 0.11.5)
OAuth	Spring Security OAuth2 Client, ScribeJava 8.3.3
Logging	SLF4J + Log4j2
Utilities	Lombok
Testing	JUnit 5, Mockito 5
Project Structure
src/main/java/com/socialMediaManager/mediaManager/
├── MediaManagerApplication.java       # Entry point
├── config/
│   ├── AppConfig.java                 # RestTemplate bean
│   └── PasswordConfig.java            # BCrypt bean
├── controllers/
│   ├── UserRegistrationController.java  # /user-registration, /user-login
│   ├── TwitterAuthController.java       # /api/twitter/authorize, /api/twitter/callback
│   └── TwitterCRUDController.java       # /mediaManager/v1/post
├── services/
│   ├── TwitterService.java              # Interface
│   ├── TwitterServiceImpl.java          # Core business logic
│   ├── CustomUserDetailsService.java    # Spring Security user loading
│   ├── JwtAuthenticationFilter.java     # JWT request filter
│   ├── SecurityConfig.java              # Security filter chain
│   ├── OAuthStateStore.java             # In-memory PKCE state store
│   └── OAuthUtil.java                   # PKCE code verifier/challenge helpers
├── repositories/
│   ├── TwitterServiceRepo.java          # UserRegistration JPA repo
│   └── TokenRepo.java                   # UserTokens JPA repo
├── entities/
│   ├── UserRegistration.java            # App user account
│   ├── UserTokens.java                  # Twitter OAuth tokens per user
│   └── PostDetails.java                 # Post history record
├── dto/
│   ├── UserRegistrationRequest/Response.java
│   ├── UserLoginRequest/Response.java
│   ├── TwitterPostRequest/Response.java
│   └── AuthResponse.java
├── mapper/
│   └── UserRegistrationMapper.java      # Request → Entity / Entity → Response
├── exceptions/
│   ├── GlobalExceptionHandler.java      # @RestControllerAdvice
│   ├── userAlreadyExistsException.java
│   ├── userDoesNotExistException.java
│   └── badCredentialsException.java
└── utility/
    └── JwtTokenProvider.java            # JWT generate / validate / parse
Getting Started
Prerequisites
Java 17+
Maven 3.8+
PostgreSQL running locally on port 5432
Setup
Clone the repository

git clone <repo-url>
cd mediaManager
Create the database

CREATE DATABASE "socailMediaManager";
Configure credentials — see Configuration

Run the application

./mvnw spring-boot:run
The server starts on http://localhost:8080.

Configuration
All configuration lives in src/main/resources/application.properties.

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/socailMediaManager
spring.datasource.username=<your-pg-username>
spring.datasource.password=<your-pg-password>

# JWT — secret must be at least 32 characters for HS256
jwt.secret=<min-32-char-secret>
jwt.expiration=3600000   # milliseconds (default: 1 hour)

# Twitter OAuth 2.0 — from developer.twitter.com
twitter.client-id=<your-twitter-client-id>
twitter.client-secret=<your-twitter-client-secret>
twitter.redirect-uri=http://localhost:8080/api/twitter/callback
The twitter.redirect-uri must exactly match the callback URL registered in your Twitter Developer App.

API Reference
User
Register
POST /user-registration
Content-Type: application/json
{
  "name": "John Doe",
  "username": "johndoe",
  "password": "secret123",
  "email": "john@example.com",
  "mobilenumber": "9876543210",
  "age": 25,
  "country": "India",
  "state": "Karnataka"
}
Responses: 201 Created | 409 Conflict (duplicate email/mobile) | 400 Bad Request (validation)

Login
POST /user-login
Content-Type: application/json
{
  "username": "johndoe",
  "password": "secret123"
}
Response 200 OK:

{
  "username": "johndoe",
  "token": "<jwt>"
}
Twitter
Start Authorization
GET /api/twitter/authorize
Authorization: Bearer <jwt>
Returns a Twitter OAuth 2.0 authorization URL. Open this URL in a browser to grant access.

Response 200 OK: https://x.com/i/oauth2/authorize?...

OAuth Callback (called by Twitter, not the client)
GET /api/twitter/callback?code=<code>&state=<state>
Exchanges the authorization code for an access token and saves it.

Response 200 OK: Twitter connected successfully

Post a Tweet
POST /mediaManager/v1/post
Authorization: Bearer <jwt>
Content-Type: application/json
{
  "data": "Hello from Social Media Manager!",
  "service": "twitter",
  "timeToPost": "2026-06-15T10:00:00"
}
Response 200 OK: Twitter API response body

Authentication Flow
Client                          Server
  |                               |
  |-- POST /user-login ---------->|
  |                               | Validates credentials (BCrypt)
  |<-- 200 OK { token: <jwt> } ---|
  |                               |
  |-- GET /api/twitter/authorize  |
  |   Authorization: Bearer <jwt> |
  |                               | JwtAuthenticationFilter validates token
  |<-- 200 OK { authUrl } --------|
Sessions are stateless — every request must carry a valid JWT in the Authorization: Bearer <token> header.
JWT expiry defaults to 1 hour (configurable via jwt.expiration).
Twitter OAuth 2.0 Flow
The app uses OAuth 2.0 with PKCE (Proof Key for Code Exchange):

1. Client → GET /api/twitter/authorize
2. Server generates code_verifier + code_challenge (SHA-256)
3. State token saved in OAuthStateStore (in-memory ConcurrentHashMap)
4. Server returns Twitter authorization URL to client
5. Client opens URL in browser and approves access
6. Twitter → GET /api/twitter/callback?code=...&state=...
7. Server validates state, retrieves code_verifier
8. Server exchanges code + code_verifier for access_token
9. Access token (+ refresh token) saved to user_tokens table
10. Subsequent tweet posts use the stored token
Database Schema
user_registration
Column	Type	Constraints
id	BIGINT	PK, auto
name	VARCHAR(30)	NOT NULL
username	VARCHAR(30)	UNIQUE, NOT NULL
password	VARCHAR	NOT NULL
email	VARCHAR	UNIQUE, NOT NULL
mobilenumber	VARCHAR(10)	UNIQUE, NOT NULL
age	INT	NOT NULL
country	VARCHAR	NOT NULL
state	VARCHAR	nullable
user_tokens
Column	Type	Constraints
id	BIGINT	PK, auto
username	VARCHAR(30)	UNIQUE, NOT NULL
token	VARCHAR(2048)	UNIQUE, NOT NULL
refresh_token	VARCHAR(2048)	UNIQUE, nullable
added_on	TIMESTAMP	auto (creation)
updated_on	TIMESTAMP	auto (update)
post_details
Column	Type	Constraints
id	BIGINT	PK, auto
username	VARCHAR(30)	NOT NULL
service	VARCHAR(20)	NOT NULL
data	VARCHAR(280)	NOT NULL
added_on	TIMESTAMP	auto (creation)
eligible_time	TIMESTAMP	NOT NULL
Error Handling
All errors are returned as JSON via GlobalExceptionHandler:

Exception	HTTP Status	Trigger
userAlreadyExistsException	409 Conflict	Duplicate email/mobile on registration
userDoesNotExistException	404 Not Found	User lookup fails
badCredentialsException	401 Unauthorized	Invalid login credentials
MethodArgumentNotValidException	400 Bad Request	Bean validation failures
RuntimeException	500 Internal Server Error	Unhandled runtime errors
Running Tests
./mvnw test
Test coverage spans:

Package	Test Class
controllers	UserRegistrationControllerTest, TwitterAuthControllerTest, TwitterCRUDControllerTest
services	TwitterServiceImplTest, CustomUserDetailsServiceTest, OAuthStateStoreTest, OAuthUtilTest
mapper	UserRegistrationMapperTest
utility	JwtTokenProviderTest
Contributing
Branch off main — use dev/<feature-name> naming
Keep changes scoped; one feature/fix per PR
Ensure ./mvnw test passes before opening a PR
Update this README if you add new endpoints, config properties, or change the DB schema
