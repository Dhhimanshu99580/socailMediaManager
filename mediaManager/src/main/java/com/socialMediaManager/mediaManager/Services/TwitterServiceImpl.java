package com.socialMediaManager.mediaManager.services;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.socialMediaManager.mediaManager.dto.UserLoginRequest;
import com.socialMediaManager.mediaManager.dto.UserLoginResponse;
import com.socialMediaManager.mediaManager.dto.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.dto.UserRegistrationResponse;
import com.socialMediaManager.mediaManager.entities.UserRegistration;
import com.socialMediaManager.mediaManager.exceptions.badCredentialsException;
import com.socialMediaManager.mediaManager.exceptions.userAlreadyExistsException;
import com.socialMediaManager.mediaManager.exceptions.userDoesNotExistException;
import com.socialMediaManager.mediaManager.mapper.UserRegistrationMapper;
import com.socialMediaManager.mediaManager.repositories.TwitterServiceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TwitterServiceImpl implements TwitterService {
    private final PasswordEncoder passwordEncoder;

    private final TwitterServiceRepo twitterServiceRepo;
    private final UserRegistrationMapper userRegistrationMapper;
    @Value("${twitter.client-id}")
    private String clientId;

    @Value("${twitter.client-secret}")
    private String clientSecret;

    @Value("${twitter.redirect-uri}")
    private String redirectUri;

    @Autowired
    public TwitterServiceImpl(TwitterServiceRepo twitterServiceRepo
            ,UserRegistrationMapper userRegistrationMapper,PasswordEncoder passwordEncoder) {
        this.twitterServiceRepo = twitterServiceRepo;
        this.userRegistrationMapper = userRegistrationMapper;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public UserRegistrationResponse processAndSaveUserRegistrationDetails(UserRegistrationRequest request) {
        Optional<UserRegistration> user = twitterServiceRepo.findByEmailAndMobileno(request.getEmail(),request.getMobilenumber());
        if(user.isPresent()) {
            throw new userAlreadyExistsException("User with email " + request.getEmail() +
                    " and mobile number " + request.getMobilenumber() + " already exists.");
        }
        twitterServiceRepo.save(userRegistrationMapper.convertToUserRegistrationEntity(request));
        return userRegistrationMapper.convertToUserRegistrationResponse(request);
    }

    @Override
    public String getAuthorizationUrl() throws Exception {
        String codeVerifier = OAuthUtil.generateCodeVerifier();
        String codeChallenge = OAuthUtil.generateCodeChallenge(codeVerifier);
        String state = "RANDOM_STATE_STRING";

        return String.format("https://twitter.com/i/oauth2/authorize?response_type=code&client_id=%s&redirect_uri=%s&scope=tweet.read%%20tweet.write&state=%s&code_challenge=%s&code_challenge_method=S256",
                clientId, redirectUri, state, codeChallenge);
    }

    @Override
    public OAuth2AccessTokenResponse getAccessToken(String code) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf("application/x-www-form-urlencoded"));
        String credentials = "clientId:clientSecret";
        String base64Creds = Base64.getEncoder().encodeToString(credentials.getBytes());
        httpHeaders.add("Authorization", "Basic " + base64Creds);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", "YOUR_CALLBACK_URL");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, httpHeaders);
        ResponseEntity<Map> response = restTemplate.exchange("https://api.x.com/oauth2/token", HttpMethod.POST, entity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        return OAuth2AccessTokenResponse.withToken(responseBody.get("access_token").toString())
                .refreshToken(responseBody.get("refresh_token").toString())
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(Long.parseLong(responseBody.get("expires_in").toString()))
                .build();

    }


}
