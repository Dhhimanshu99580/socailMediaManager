package com.socialMediaManager.mediaManager.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialMediaManager.mediaManager.dto.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.dto.UserRegistrationResponse;
import com.socialMediaManager.mediaManager.entities.UserRegistration;
import com.socialMediaManager.mediaManager.services.TwitterService;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserRegistrationController.class)
public class UserRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TwitterService twitterService;
    @Autowired
    private ObjectMapper objectMapper;
    UserRegistrationController userRegistrationController;

    @BeforeEach
    void setUp() {
        userRegistrationController = new UserRegistrationController();
    }

    @Test
    void testUserLogin() {
        assertEquals(4,4);
    }

    @Test
    public void testUserRegistration() throws Exception {
        UserRegistrationRequest userRegistration = new UserRegistrationRequest();
        userRegistration.setUsername("Ghost");
        userRegistration.setName("Himanshu");
        userRegistration.setAge(25);
        userRegistration.setEmail("himanshu23ranjan@gmail.com");
        userRegistration.setMobilenumber("9958490105");
        userRegistration.setPassword("Random@123");
        userRegistration.setCountry("India");

        UserRegistrationResponse userRegistrationResponse = new UserRegistrationResponse();
        userRegistrationResponse.setEmail("himanshu23ranjan@gmail.com");
        userRegistrationResponse.setName("Himanshu");
        userRegistrationResponse.setMobileno("9958490105");

        when(twitterService.processAndSaveUserRegistrationDetails(userRegistration)).
                thenReturn(userRegistrationResponse);
        mockMvc.perform(post("/user-registartion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistration)))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.email").value("himanshu23ranjan@gmail.com"))
                .andExpect((ResultMatcher) jsonPath("$.name").value("Himanshu"))
                .andExpect((ResultMatcher) jsonPath("$.mobileno").value("9958490204"));

    }
}
