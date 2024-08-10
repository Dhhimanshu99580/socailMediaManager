package com.socialMediaManager.mediaManager.Controllers;

import com.socialMediaManager.mediaManager.DTO.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.DTO.UserRegistrationResponse;
import com.socialMediaManager.mediaManager.Entities.UserRegistration;
import com.socialMediaManager.mediaManager.Services.TwitterServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserRegistrationController {
    @Autowired
    private TwitterServiceImpl twitterService;
    @PostMapping("/user-registartion")
    public UserRegistrationResponse saveRegistartionDetails(@RequestBody @Valid  UserRegistrationRequest request) {
        return twitterService.processAndSaveUserRegistrationDetails(request);
    }
}
