package com.socialMediaManager.mediaManager.services;

import com.socialMediaManager.mediaManager.dto.TwitterPostRequest;
import com.socialMediaManager.mediaManager.dto.TwitterPostResponse;
import com.socialMediaManager.mediaManager.dto.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.dto.UserRegistrationResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

public interface TwitterService {
    UserRegistrationResponse processAndSaveUserRegistrationDetails(UserRegistrationRequest request);
    String getAuthorizationUrl(String username) throws Exception;
    OAuth2AccessTokenResponse getAccessToken(String code, String state) throws Exception;
    TwitterPostResponse postOnTwitter(TwitterPostRequest request);
}
