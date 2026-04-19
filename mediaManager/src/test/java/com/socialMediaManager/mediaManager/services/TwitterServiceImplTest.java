package com.socialMediaManager.mediaManager.services;

import com.socialMediaManager.mediaManager.dto.TwitterPostRequest;
import com.socialMediaManager.mediaManager.dto.TwitterPostResponse;
import com.socialMediaManager.mediaManager.dto.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.dto.UserRegistrationResponse;
import com.socialMediaManager.mediaManager.entities.UserRegistration;
import com.socialMediaManager.mediaManager.entities.UserTokens;
import com.socialMediaManager.mediaManager.exceptions.userAlreadyExistsException;
import com.socialMediaManager.mediaManager.mapper.UserRegistrationMapper;
import com.socialMediaManager.mediaManager.repositories.TokenRepo;
import com.socialMediaManager.mediaManager.repositories.TwitterServiceRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentMatchers;

@ExtendWith(MockitoExtension.class)
class TwitterServiceImplTest {

    @Mock private TwitterServiceRepo twitterServiceRepo;
    @Mock private UserRegistrationMapper userRegistrationMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenRepo tokenRepo;
    @Mock private OAuthStateStore stateStore;
    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private TwitterServiceImpl twitterService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(twitterService, "clientId", "testClientId");
        ReflectionTestUtils.setField(twitterService, "clientSecret", "testClientSecret");
        ReflectionTestUtils.setField(twitterService, "redirectUri", "http://localhost:8080/api/twitter/callback");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void register_newUser_savesAndReturnsResponse() {
        UserRegistrationRequest request = buildRegistrationRequest();
        UserRegistration entity = new UserRegistration();
        UserRegistrationResponse expectedResponse = new UserRegistrationResponse();

        when(twitterServiceRepo.findByEmailAndMobilenumber(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(userRegistrationMapper.convertToUserRegistrationEntity(request)).thenReturn(entity);
        when(userRegistrationMapper.convertToUserRegistrationResponse(request)).thenReturn(expectedResponse);

        UserRegistrationResponse result = twitterService.processAndSaveUserRegistrationDetails(request);

        assertNotNull(result);
        verify(twitterServiceRepo).save(entity);
    }

    @Test
    void register_existingUser_throwsUserAlreadyExistsException() {
        UserRegistrationRequest request = buildRegistrationRequest();
        when(twitterServiceRepo.findByEmailAndMobilenumber(anyString(), anyString()))
                .thenReturn(Optional.of(new UserRegistration()));

        assertThrows(userAlreadyExistsException.class,
                () -> twitterService.processAndSaveUserRegistrationDetails(request));

        verify(twitterServiceRepo, never()).save(any());
    }

    // --- OAuth Authorization URL ---

    @Test
    void getAuthorizationUrl_containsClientId() throws Exception {
        String url = twitterService.getAuthorizationUrl("himanshu");
        assertTrue(url.contains("client_id=testClientId"));
    }

    @Test
    void getAuthorizationUrl_containsS256Method() throws Exception {
        String url = twitterService.getAuthorizationUrl("himanshu");
        assertTrue(url.contains("code_challenge_method=S256"));
    }

    @Test
    void getAuthorizationUrl_containsResponseTypeCode() throws Exception {
        String url = twitterService.getAuthorizationUrl("himanshu");
        assertTrue(url.contains("response_type=code"));
    }

    @Test
    void getAuthorizationUrl_savesStateInStore() throws Exception {
        twitterService.getAuthorizationUrl("himanshu");
        verify(stateStore).save(anyString(), anyString(), eq("himanshu"));
    }



    @Test
    void getAccessToken_invalidState_throwsException() {
        when(stateStore.isValid("badState")).thenReturn(false);

        assertThrows(Exception.class,
                () -> twitterService.getAccessToken("code", "badState"));

        verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(Map.class));
    }

    @Test
    void getAccessToken_validState_savesTokenAndReturnsResponse() throws Exception {
        when(stateStore.isValid("validState")).thenReturn(true);
        when(stateStore.getCodeVerifier("validState")).thenReturn("codeVerifier");
        when(stateStore.getUsername("validState")).thenReturn("himanshu");

        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "twitter_access_token");
        tokenResponse.put("refresh_token", "twitter_refresh_token");
        tokenResponse.put("expires_in", 7200);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));
        when(tokenRepo.findByUsername("himanshu")).thenReturn(Optional.of(new UserTokens()));

        OAuth2AccessTokenResponse response = twitterService.getAccessToken("authCode", "validState");

        assertNotNull(response);
        assertEquals("twitter_access_token", response.getAccessToken().getTokenValue());
        verify(tokenRepo).save(any(UserTokens.class));
    }

    @Test
    void getAccessToken_removesStateAfterUse() throws Exception {
        when(stateStore.isValid("validState")).thenReturn(true);
        when(stateStore.getCodeVerifier("validState")).thenReturn("verifier");
        when(stateStore.getUsername("validState")).thenReturn("himanshu");

        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "token");
        tokenResponse.put("refresh_token", "refresh");
        tokenResponse.put("expires_in", 7200);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));
        when(tokenRepo.findByUsername("himanshu")).thenReturn(Optional.empty());

        twitterService.getAccessToken("code", "validState");

        verify(stateStore).remove("validState");
    }


    @Test
    void postOnTwitter_noTokenForUser_throwsException() {
        mockSecurityContext("himanshu");
        when(tokenRepo.findByUsername("himanshu")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> twitterService.postOnTwitter(buildPostRequest()));
    }

    @Test
    void postOnTwitter_success_returnsResponse() {
        mockSecurityContext("himanshu");
        UserTokens tokens = new UserTokens();
        tokens.setToken("validTwitterToken");
        when(tokenRepo.findByUsername("himanshu")).thenReturn(Optional.of(tokens));

        TwitterPostResponse expectedResponse = new TwitterPostResponse();
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TwitterPostResponse.class)))
                .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        TwitterPostResponse result = twitterService.postOnTwitter(buildPostRequest());

        assertNotNull(result);
    }


    private void mockSecurityContext(String username) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.getName()).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private UserRegistrationRequest buildRegistrationRequest() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("Himanshu");
        request.setUsername("himanshu99");
        request.setPassword("Secret@123");
        request.setEmail("him@example.com");
        request.setMobilenumber("9958490105");
        request.setCountry("India");
        request.setAge(25);
        return request;
    }

    private TwitterPostRequest buildPostRequest() {
        TwitterPostRequest request = new TwitterPostRequest();
        request.setData("Hello Twitter!");
        request.setService("TWITTER");
        return request;
    }
}
