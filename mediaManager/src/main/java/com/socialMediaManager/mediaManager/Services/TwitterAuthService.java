package com.socialMediaManager.mediaManager.services;

import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

public interface TwitterAuthService {
    String getAuthorizationUrl(String username) throws Exception;
    OAuth2AccessTokenResponse getAccessToken(String code, String state) throws Exception;
    String refreshAccessToken(String username) throws Exception;
}
