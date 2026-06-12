package com.socialMediaManager.mediaManager.services;

import com.socialMediaManager.mediaManager.entities.UserTokens;
import com.socialMediaManager.mediaManager.repositories.TokenRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwitterAuthServiceImplTest {

    @Mock private TokenRepo tokenRepo;
    @Mock private OAuthStateStore stateStore;
    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private TwitterAuthServiceImpl twitterAuthService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(twitterAuthService, "clientId", "testClientId");
        ReflectionTestUtils.setField(twitterAuthService, "clientSecret", "testClientSecret");
        ReflectionTestUtils.setField(twitterAuthService, "redirectUri", "http://localhost:8080/api/v1/twitter/callback");
    }

    @Test
    void getAuthorizationUrl_containsClientId() throws Exception {
        String url = twitterAuthService.getAuthorizationUrl("himanshu");
        assertTrue(url.contains("client_id=testClientId"));
    }

    @Test
    void getAuthorizationUrl_containsS256Method() throws Exception {
        String url = twitterAuthService.getAuthorizationUrl("himanshu");
        assertTrue(url.contains("code_challenge_method=S256"));
    }

    @Test
    void getAuthorizationUrl_containsResponseTypeCode() throws Exception {
        String url = twitterAuthService.getAuthorizationUrl("himanshu");
        assertTrue(url.contains("response_type=code"));
    }

    @Test
    void getAuthorizationUrl_savesStateInStore() throws Exception {
        twitterAuthService.getAuthorizationUrl("himanshu");
        verify(stateStore).save(anyString(), anyString(), eq("himanshu"));
    }

    @Test
    void getAccessToken_invalidState_throwsException() {
        when(stateStore.isValid("badState")).thenReturn(false);

        assertThrows(Exception.class,
                () -> twitterAuthService.getAccessToken("code", "badState"));
        verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(Map.class));
    }

    @Test
    void getAccessToken_validState_savesTokenAndReturnsResponse() throws Exception {
        when(stateStore.isValid("validState")).thenReturn(true);
        when(stateStore.getCodeVerifier("validState")).thenReturn("codeVerifier");
        when(stateStore.getUsername("validState")).thenReturn("himanshu");

        Map<String, Object> tokenResponse = buildTokenResponse();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));
        when(tokenRepo.findByUsername("himanshu")).thenReturn(Optional.of(new UserTokens()));

        OAuth2AccessTokenResponse response = twitterAuthService.getAccessToken("authCode", "validState");

        assertNotNull(response);
        assertEquals("twitter_access_token", response.getAccessToken().getTokenValue());
        verify(tokenRepo).save(any(UserTokens.class));
    }

    @Test
    void getAccessToken_removesStateAfterUse() throws Exception {
        when(stateStore.isValid("validState")).thenReturn(true);
        when(stateStore.getCodeVerifier("validState")).thenReturn("verifier");
        when(stateStore.getUsername("validState")).thenReturn("himanshu");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                .thenReturn(new ResponseEntity<>(buildTokenResponse(), HttpStatus.OK));
        when(tokenRepo.findByUsername("himanshu")).thenReturn(Optional.empty());

        twitterAuthService.getAccessToken("code", "validState");

        verify(stateStore).remove("validState");
    }

    @Test
    void refreshAccessToken_noTokensForUser_throwsException() {
        when(tokenRepo.findByUsername("himanshu")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> twitterAuthService.refreshAccessToken("himanshu"));
    }

    @Test
    void refreshAccessToken_noRefreshToken_throwsException() {
        UserTokens tokens = new UserTokens();
        tokens.setUsername("himanshu");
        tokens.setToken("old_access_token");
        // refreshToken is null
        when(tokenRepo.findByUsername("himanshu")).thenReturn(Optional.of(tokens));

        assertThrows(RuntimeException.class,
                () -> twitterAuthService.refreshAccessToken("himanshu"));
    }

    @Test
    void refreshAccessToken_success_updatesAndReturnsNewToken() throws Exception {
        UserTokens tokens = new UserTokens();
        tokens.setUsername("himanshu");
        tokens.setToken("old_access_token");
        tokens.setRefreshToken("valid_refresh_token");
        when(tokenRepo.findByUsername("himanshu")).thenReturn(Optional.of(tokens));

        Map<String, Object> tokenResponse = buildTokenResponse();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

        String newToken = twitterAuthService.refreshAccessToken("himanshu");

        assertEquals("twitter_access_token", newToken);
        verify(tokenRepo).save(any(UserTokens.class));
    }

    private Map<String, Object> buildTokenResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", "twitter_access_token");
        response.put("refresh_token", "twitter_refresh_token");
        response.put("expires_in", 7200);
        return response;
    }
}
