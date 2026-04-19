package com.socialMediaManager.mediaManager.controllers;

import com.socialMediaManager.mediaManager.services.JwtAuthenticationFilter;
import com.socialMediaManager.mediaManager.services.TwitterService;
import com.socialMediaManager.mediaManager.utility.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = TwitterAuthController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class}
)
class TwitterAuthControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private TwitterService twitterService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void authorize_missingAuthHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/twitter/authorize"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authorize_headerWithoutBearerPrefix_returns401() throws Exception {
        mockMvc.perform(get("/api/twitter/authorize")
                        .header("Authorization", "Basic sometoken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authorize_invalidToken_returns401() throws Exception {
        when(jwtTokenProvider.validateToken("badtoken")).thenReturn(false);

        mockMvc.perform(get("/api/twitter/authorize")
                        .header("Authorization", "Bearer badtoken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authorize_validToken_returnsTwitterAuthUrl() throws Exception {
        when(jwtTokenProvider.validateToken("validtoken")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("validtoken")).thenReturn("himanshu");
        when(twitterService.getAuthorizationUrl("himanshu"))
                .thenReturn("https://x.com/i/oauth2/authorize?response_type=code&client_id=testId");

        mockMvc.perform(get("/api/twitter/authorize")
                        .header("Authorization", "Bearer validtoken"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("https://x.com/i/oauth2/authorize")));
    }

    @Test
    void authorize_serviceThrowsException_returns500() throws Exception {
        when(jwtTokenProvider.validateToken("validtoken")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("validtoken")).thenReturn("himanshu");
        when(twitterService.getAuthorizationUrl("himanshu")).thenThrow(new RuntimeException("Twitter error"));

        mockMvc.perform(get("/api/twitter/authorize")
                        .header("Authorization", "Bearer validtoken"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void callback_validCodeAndState_returns200() throws Exception {
        when(twitterService.getAccessToken("authCode", "validState")).thenReturn(null);

        mockMvc.perform(get("/api/twitter/callback")
                        .param("code", "authCode")
                        .param("state", "validState"))
                .andExpect(status().isOk())
                .andExpect(content().string("Twitter connected successfully"));
    }

    @Test
    void callback_invalidState_returns401() throws Exception {
        when(twitterService.getAccessToken("authCode", "badState"))
                .thenThrow(new Exception("Invalid or expired OAuth state parameter"));

        mockMvc.perform(get("/api/twitter/callback")
                        .param("code", "authCode")
                        .param("state", "badState"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid or expired")));
    }
}
