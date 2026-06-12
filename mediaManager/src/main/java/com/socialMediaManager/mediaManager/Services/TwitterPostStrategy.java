package com.socialMediaManager.mediaManager.services;

import com.socialMediaManager.mediaManager.dto.TwitterPostResponse;
import com.socialMediaManager.mediaManager.entities.UserTokens;
import com.socialMediaManager.mediaManager.repositories.TokenRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class TwitterPostStrategy implements PlatformPostStrategy {

    private static final Logger log = LoggerFactory.getLogger(TwitterPostStrategy.class);
    private static final String PLATFORM = "twitter";
    private static final String TWITTER_API_URL = "https://api.x.com/2/tweets";

    private final TokenRepo tokenRepo;
    private final TwitterAuthService twitterAuthService;
    private final RestTemplate restTemplate;

    @Autowired
    public TwitterPostStrategy(TokenRepo tokenRepo, TwitterAuthService twitterAuthService, RestTemplate restTemplate) {
        this.tokenRepo = tokenRepo;
        this.twitterAuthService = twitterAuthService;
        this.restTemplate = restTemplate;
    }

    @Override
    public String getPlatformName() {
        return PLATFORM;
    }

    @Override
    public TwitterPostResponse post(String text, String username) {
        String accessToken = tokenRepo.findByUsername(username)
                .map(UserTokens::getToken)
                .orElseThrow(() -> new RuntimeException(
                        "No Twitter token found for user: " + username + ". Please connect your Twitter account first."));
        try {
            return callTwitterApi(text, accessToken);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                log.warn("Twitter token expired for {}. Attempting refresh.", username);
                try {
                    String newToken = twitterAuthService.refreshAccessToken(username);
                    return callTwitterApi(text, newToken);
                } catch (Exception refreshEx) {
                    log.error("Token refresh failed for {}: {}", username, refreshEx.getMessage());
                    throw new RuntimeException("Twitter token expired and refresh failed. Please re-authorize.", refreshEx);
                }
            }
            log.error("Twitter API client error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    private TwitterPostResponse callTwitterApi(String text, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of("text", text);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<TwitterPostResponse> response = restTemplate.postForEntity(
                TWITTER_API_URL, entity, TwitterPostResponse.class);
        return response.getBody();
    }
}
