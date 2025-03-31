package com.socialMediaManager.mediaManager.repositories;

import com.socialMediaManager.mediaManager.entities.UserTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

public interface TokenRepo extends JpaRepository<UserTokens,Long> {

//    static Object findByAccessToken(OAuth2AccessTokenResponse accessToken) {
//        return
//    }
}
