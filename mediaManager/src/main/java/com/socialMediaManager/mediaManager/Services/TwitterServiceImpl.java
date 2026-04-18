package com.socialMediaManager.mediaManager.services;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.Response;
import com.socialMediaManager.mediaManager.dto.TwitterPostRequest;
import com.socialMediaManager.mediaManager.dto.TwitterPostResponse;
import com.socialMediaManager.mediaManager.dto.UserLoginRequest;
import com.socialMediaManager.mediaManager.dto.UserLoginResponse;
import com.socialMediaManager.mediaManager.dto.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.dto.UserRegistrationResponse;
import com.socialMediaManager.mediaManager.entities.PostDetails;
import com.socialMediaManager.mediaManager.entities.UserRegistration;
import com.socialMediaManager.mediaManager.entities.UserTokens;
import com.socialMediaManager.mediaManager.exceptions.badCredentialsException;
import com.socialMediaManager.mediaManager.exceptions.userAlreadyExistsException;
import com.socialMediaManager.mediaManager.exceptions.userDoesNotExistException;
import com.socialMediaManager.mediaManager.mapper.UserRegistrationMapper;
import com.socialMediaManager.mediaManager.repositories.TokenRepo;
import com.socialMediaManager.mediaManager.repositories.TwitterServiceRepo;
import com.socialMediaManager.mediaManager.utility.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TwitterServiceImpl implements TwitterService {
    private final PasswordEncoder passwordEncoder;
    private final String TWITTER_API_URL = "https://api.x.com/2/tweets";

    private final TwitterServiceRepo twitterServiceRepo;
    private final UserRegistrationMapper userRegistrationMapper;
    private final UserTokens userToken;
    private final TokenRepo tokenRepo;
    private final PostDetails postDetails;
    @Value("${twitter.client-id}")
    private String clientId;

    @Value("${twitter.client-secret}")
    private String clientSecret;

    @Value("${twitter.redirect-uri}")
    private String redirectUri;

    @Autowired
    public TwitterServiceImpl(TwitterServiceRepo twitterServiceRepo
            , UserRegistrationMapper userRegistrationMapper, PasswordEncoder passwordEncoder, TokenRepo tokenRepo, UserTokens userToken, PostDetails postDetails) {
        this.twitterServiceRepo = twitterServiceRepo;
        this.userRegistrationMapper = userRegistrationMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepo = tokenRepo;
        this.userToken = userToken;
        this.postDetails = postDetails;
    }
    @Autowired
    private TwitterService twitterService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RestTemplate restTemplate;
    @Override
    public UserRegistrationResponse processAndSaveUserRegistrationDetails(UserRegistrationRequest request) {
        Optional<UserRegistration> user = twitterServiceRepo.findByEmailAndMobile(request.getEmail(),request.getMobilenumber());
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

        //generate unique state for CSRF protection
        String state = UUID.randomUUID().toString();

        //Save the code verifier and state in DB
        //This will be used later when exchanging the code for tokens
        saveStateAndCodeVerifier(state, codeVerifier);

        String baseUrl = "https://x.com/i/oauth2/authorize";
        String cientId = "Replace it with you original client ID from X developers forum";
        String redirectUri = URLEncoder.encode("MY_REDIRECT_URI", StandardCharsets.UTF_8);
        String scope = URLEncoder.encode("tweet.read tweet.write users.read offline.access", StandardCharsets.UTF_8);
        String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);
        String encodedCodeChallenge = URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8);

        // where are we using this url ?? or do we even need this?
        // String url = "https://x.com/i/oauth2/authorize?response_type=code&client_id=YOUR_CLIENT_ID&redirect_uri=YOUR_REDIRECT_URI&scope=tweet.read%20tweet.write%20users.read%20offline.acces&state=YOUR_UNIQUE_STATE&code_challenge=YOUR_CODE_CHALLENGE&code_challenge_method=plain";


        return String.format(
                "%s?response_type=code&client_id=%s&redirect_uri=%s&scope=%s&state=%s&code_challenge=%s&code_challenge_method=S256",
                baseUrl, clientId, redirectUri, scope, encodedState, encodedCodeChallenge
        );
    }
    public void saveStateAndCodeVerifier(String state,String codeVerifier) {
        //save these to DB and retrieve for verification with the code we receive in our callback URI
    }

    @Override
    public OAuth2AccessTokenResponse getAccessToken(String code,String state) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf("application/x-www-form-urlencoded"));
        String credentials = "clientId:clientSecret";
        String base64Creds = Base64.getEncoder().encodeToString(credentials.getBytes());
        httpHeaders.add("Authorization", "Basic " + base64Creds);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("grant_type", "authorization_code"); // need original authorization_code
        body.add("redirect_uri", "CALLBACK_URL");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, httpHeaders);
        ResponseEntity<Map> response = restTemplate.exchange("https://api.x.com/oauth2/token", HttpMethod.POST, entity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if(responseBody == null || !responseBody.containsKey("access_token")) {
            throw new Exception("Didn't get proper response from the Authorization Server");
        }
        uploadAccessToken(responseBody); // need to save this with proper encryption
        return OAuth2AccessTokenResponse.withToken(responseBody.get("access_token").toString())
                .refreshToken(responseBody.get("refresh_token").toString())
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(Long.parseLong(responseBody.get("expires_in").toString()))
                .build();

    }
    public void uploadAccessToken(Map<String,Object> accessTokenResponse) throws Exception {
        String token = accessTokenResponse.get("access_token").toString();
        String refreshToken = accessTokenResponse.get("refresh_token").toString();
        int expiresIn = (int)accessTokenResponse.get("expires_in");
        String username = jwtTokenProvider.getUsernameFromToken(token);
        Optional<UserRegistration> user  = twitterServiceRepo.findByUsername(username);
        if(!user.isPresent()) {
            return;
        }
        userToken.setUsername(user.get().getUsername());
        userToken.setToken(token);
        userToken.setRefreshToken(refreshToken);
        tokenRepo.save(userToken);
    }
    public TwitterPostResponse postOnTwitter(TwitterPostRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth("changeMePlease"); // change here
        httpHeaders.set("Content-Type","application/json");

        try {
            HttpEntity<TwitterPostRequest> httpEntity = new HttpEntity<>(request,httpHeaders);
            ResponseEntity<TwitterPostResponse> responseEntity = restTemplate.postForEntity(TWITTER_API_URL,httpEntity,TwitterPostResponse.class);
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            if(e.getStatusCode().is4xxClientError()) {
                System.err.println("Client Error" + e.getStatusCode()+ "-" + e.getStatusText());
                System.err.println("ResponseBody" + e.getResponseBodyAsString());
            } else {
                System.err.println("Unexpected error" + e.getStatusCode());
            }
        } catch (HttpServerErrorException e) {
            System.err.println("Server Error" +e.getStatusCode()+ "-" + e.getStatusText());
            System.err.println("ResponseBody" + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Some error occured" +e.getMessage());
        }
        return null;
    }
//        public void saveTheDataForFuturePost(TwitterPostRequest request) {
//            //Save these details in db in proper format.So that when cron runs it fetch these to POST on respective service
//            //Also save the service like X,Meta,Google etc..
//            UserTokens user = TokenRepo.findByAccessToken(getAccessToken()).orElse{
//                //No user exist with this token
//            }
//            if(request.getTimeToPost< LocalDateTime.now()) {
//                System.err.println("Time to post can't be less than current time");
//            }
//            postDetails.setUsername(user.getUsername());
//            postDetails.setService(request.getService());
//            postDetails.setEligibleTime(request.getTimeToPost());
//            postDetails.setData(request.getData());
//        }
}
