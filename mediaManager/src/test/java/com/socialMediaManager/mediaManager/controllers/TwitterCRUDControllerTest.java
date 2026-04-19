package com.socialMediaManager.mediaManager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialMediaManager.mediaManager.dto.TwitterPostRequest;
import com.socialMediaManager.mediaManager.dto.TwitterPostResponse;
import com.socialMediaManager.mediaManager.services.JwtAuthenticationFilter;
import com.socialMediaManager.mediaManager.services.TwitterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = TwitterCRUDController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class}
)
class TwitterCRUDControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private TwitterService twitterService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void postOnTwitter_validRequest_returns200() throws Exception {
        when(twitterService.postOnTwitter(any(TwitterPostRequest.class)))
                .thenReturn(new TwitterPostResponse());

        TwitterPostRequest request = new TwitterPostRequest();
        request.setData("Hello Twitter!");
        request.setService("TWITTER");
        request.setTimeToPost(LocalDateTime.now().plusHours(1));

        mockMvc.perform(post("/mediaManager/v1/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void postOnTwitter_missingData_returns400() throws Exception {
        TwitterPostRequest request = new TwitterPostRequest();
        // data is null — @NotNull should trigger validation
        request.setService("TWITTER");

        mockMvc.perform(post("/mediaManager/v1/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postOnTwitter_missingService_returns400() throws Exception {
        TwitterPostRequest request = new TwitterPostRequest();
        request.setData("Hello Twitter!");
        // service is null — @NotNull should trigger validation

        mockMvc.perform(post("/mediaManager/v1/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postOnTwitter_serviceThrowsRuntimeException_returns500() throws Exception {
        when(twitterService.postOnTwitter(any()))
                .thenThrow(new RuntimeException("No Twitter token found"));

        TwitterPostRequest request = new TwitterPostRequest();
        request.setData("Hello Twitter!");
        request.setService("TWITTER");

        mockMvc.perform(post("/mediaManager/v1/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}
