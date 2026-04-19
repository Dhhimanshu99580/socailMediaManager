package com.socialMediaManager.mediaManager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialMediaManager.mediaManager.dto.UserLoginRequest;
import com.socialMediaManager.mediaManager.dto.UserLoginResponse;
import com.socialMediaManager.mediaManager.dto.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.dto.UserRegistrationResponse;
import com.socialMediaManager.mediaManager.exceptions.userAlreadyExistsException;
import com.socialMediaManager.mediaManager.services.CustomUserDetailsService;
import com.socialMediaManager.mediaManager.services.JwtAuthenticationFilter;
import com.socialMediaManager.mediaManager.services.TwitterServiceImpl;
import com.socialMediaManager.mediaManager.utility.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = UserRegistrationController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class}
)
class UserRegistrationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private TwitterServiceImpl twitterService;
    @MockBean private AuthenticationManager authenticationManager;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private CustomUserDetailsService userDetailsService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void registerUser_validRequest_returns201() throws Exception {
        UserRegistrationRequest request = buildRegistrationRequest();
        UserRegistrationResponse response = new UserRegistrationResponse();
        response.setName("Himanshu");
        response.setEmail("him@example.com");
        response.setMobileno("9958490105");

        when(twitterService.processAndSaveUserRegistrationDetails(any())).thenReturn(response);

        mockMvc.perform(post("/user-registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("him@example.com"))
                .andExpect(jsonPath("$.name").value("Himanshu"));
    }

    @Test
    void registerUser_duplicateUser_returns409() throws Exception {
        when(twitterService.processAndSaveUserRegistrationDetails(any()))
                .thenThrow(new userAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/user-registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRegistrationRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User already exists"));
    }

    @Test
    void userLogin_validCredentials_returnsTokenAndUsername() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(jwtTokenProvider.generateToken("himanshu99")).thenReturn("mocked.jwt.token");

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUsername("himanshu99");
        loginRequest.setPassword("Secret@123");

        mockMvc.perform(post("/user-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.username").value("himanshu99"));
    }

    @Test
    void userLogin_invalidCredentials_returns401() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUsername("himanshu99");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/user-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
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
}
