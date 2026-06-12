package com.socialMediaManager.mediaManager.services;

import com.socialMediaManager.mediaManager.dto.TwitterPostResponse;
import com.socialMediaManager.mediaManager.entities.UserTokens;
import com.socialMediaManager.mediaManager.repositories.TokenRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwitterPostStrategyTest {

    @Mock private TokenRepo tokenRepo;
    @Mock private TwitterAuthService twitterAuthService;
    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private TwitterPostStrategy twitterPostStrategy;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getPlatformName_returnsTwitter() {
        assertEquals("twitter", twitterPostStrategy.getPlatformName());
    }

    @Test
    void post_noTokenForUser_throwsException() {
        when(tokenRepo.findByUsername("himanshu")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> twitterPostStrategy.post("Hello!", "himanshu"));
    }

    @Test
    void post_success_returnsResponse() {
        UserTokens tokens = new UserTokens();
        tokens.setToken("validToken");
        when(tokenRepo.findByUsername("himanshu")).thenReturn(Optional.of(tokens));

        TwitterPostResponse expected = new TwitterPostResponse();
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TwitterPostResponse.class)))
                .thenReturn(new ResponseEntity<>(expected, HttpStatus.OK));

        TwitterPostResponse result = twitterPostStrategy.post("Hello!", "himanshu");

        assertNotNull(result);
    }

    @Test
    void post_tokenExpired_refreshesAndRetries() throws Exception {
        UserTokens tokens = new UserTokens();
        tokens.setToken("expiredToken");
        when(tokenRepo.findByUsername("himanshu")).thenReturn(Optional.of(tokens));

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TwitterPostResponse.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null))
                .thenReturn(new ResponseEntity<>(new TwitterPostResponse(), HttpStatus.OK));

        when(twitterAuthService.refreshAccessToken("himanshu")).thenReturn("newToken");

        TwitterPostResponse result = twitterPostStrategy.post("Hello!", "himanshu");

        assertNotNull(result);
        verify(twitterAuthService).refreshAccessToken("himanshu");
    }

    @Test
    void post_tokenExpiredAndRefreshFails_throwsException() throws Exception {
        UserTokens tokens = new UserTokens();
        tokens.setToken("expiredToken");
        when(tokenRepo.findByUsername("himanshu")).thenReturn(Optional.of(tokens));

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TwitterPostResponse.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));

        when(twitterAuthService.refreshAccessToken("himanshu"))
                .thenThrow(new RuntimeException("No refresh token"));

        assertThrows(RuntimeException.class,
                () -> twitterPostStrategy.post("Hello!", "himanshu"));
    }
}
