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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TwitterServiceImpl implements TwitterService {

    private static final Logger log = LoggerFactory.getLogger(TwitterServiceImpl.class);
    private static final String TWITTER_API_URL = "https://api.x.com/2/tweets";
    private static final String TWITTER_TOKEN_URL = "https://api.x.com/2/oauth2/token";

    private final PasswordEncoder passwordEncoder;
    private final TwitterServiceRepo twitterServiceRepo;
    private final UserRegistrationMapper userRegistrationMapper;
    private final TokenRepo tokenRepo;
    private final OAuthStateStore stateStore;
    private final RestTemplate restTemplate;

    @Value("${twitter.client-id}")
    private String clientId;

    @Value("${twitter.client-secret}")
    private String clientSecret;

    @Value("${twitter.redirect-uri}")
    private String redirectUri;

    @Autowired
    public TwitterServiceImpl(TwitterServiceRepo twitterServiceRepo,
                               UserRegistrationMapper userRegistrationMapper,
                               PasswordEncoder passwordEncoder,
                               TokenRepo tokenRepo,
                               OAuthStateStore stateStore,
                               RestTemplate restTemplate) {
        this.twitterServiceRepo = twitterServiceRepo;
        this.userRegistrationMapper = userRegistrationMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepo = tokenRepo;
        this.stateStore = stateStore;
        this.restTemplate = restTemplate;
    }

    @Override
    public UserRegistrationResponse processAndSaveUserRegistrationDetails(UserRegistrationRequest request) {
        Optional<UserRegistration> existing = twitterServiceRepo.findByEmailAndMobilenumber(request.getEmail(), request.getMobilenumber());
        if (existing.isPresent()) {
            throw new userAlreadyExistsException("User with email " + request.getEmail()
                    + " and mobile number " + request.getMobilenumber() + " already exists.");
        }
        twitterServiceRepo.save(userRegistrationMapper.convertToUserRegistrationEntity(request));
        return userRegistrationMapper.convertToUserRegistrationResponse(request);
    }

    @Override
    public String getAuthorizationUrl(String username) throws Exception {
        String codeVerifier = OAuthUtil.generateCodeVerifier();
        String codeChallenge = OAuthUtil.generateCodeChallenge(codeVerifier);
        String state = UUID.randomUUID().toString();

        stateStore.save(state, codeVerifier, username);

        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String scope = URLEncoder.encode("tweet.read tweet.write users.read offline.access", StandardCharsets.UTF_8);
        String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);
        String encodedCodeChallenge = URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8);

        return String.format(
                "https://x.com/i/oauth2/authorize?response_type=code&client_id=%s&redirect_uri=%s&scope=%s&state=%s&code_challenge=%s&code_challenge_method=S256",
                clientId, encodedRedirectUri, scope, encodedState, encodedCodeChallenge
        );
    }

    @Override
    public OAuth2AccessTokenResponse getAccessToken(String code, String state) throws Exception {
        if (!stateStore.isValid(state)) {
            throw new Exception("Invalid or expired OAuth state parameter");
        }

        String codeVerifier = stateStore.getCodeVerifier(state);
        String username = stateStore.getUsername(state);
        stateStore.remove(state);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String credentials = clientId + ":" + clientSecret;
        String base64Creds = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + base64Creds);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", redirectUri);
        body.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                TWITTER_TOKEN_URL, HttpMethod.POST, entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new Exception("Failed to obtain access token from Twitter");
        }

        saveTwitterTokens(username, responseBody);

        return OAuth2AccessTokenResponse.withToken(responseBody.get("access_token").toString())
                .refreshToken(responseBody.containsKey("refresh_token") ? responseBody.get("refresh_token").toString() : null)
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(Long.parseLong(responseBody.get("expires_in").toString()))
                .build();
    }

    private void saveTwitterTokens(String username, Map<String, Object> tokenResponse) {
        UserTokens tokens = tokenRepo.findByUsername(username).orElse(new UserTokens());
        tokens.setUsername(username);
        tokens.setToken(tokenResponse.get("access_token").toString());
        if (tokenResponse.containsKey("refresh_token")) {
            tokens.setRefreshToken(tokenResponse.get("refresh_token").toString());
        }
        tokenRepo.save(tokens);
    }

    @Override
    public TwitterPostResponse postOnTwitter(TwitterPostRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String accessToken = tokenRepo.findByUsername(username)
                .map(UserTokens::getToken)
                .orElseThrow(() -> new RuntimeException("No Twitter token found for user: " + username + ". Please connect your Twitter account first."));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            HttpEntity<TwitterPostRequest> httpEntity = new HttpEntity<>(request, headers);
            ResponseEntity<TwitterPostResponse> responseEntity = restTemplate.postForEntity(TWITTER_API_URL, httpEntity, TwitterPostResponse.class);
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Twitter API client error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (HttpServerErrorException e) {
            log.error("Twitter API server error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }
}
