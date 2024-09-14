package com.socialMediaManager.mediaManager.mapper;

import com.socialMediaManager.mediaManager.dto.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.dto.UserRegistrationResponse;
import com.socialMediaManager.mediaManager.entities.UserRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserRegistrationMapper {


    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserRegistrationMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public UserRegistration convertToUserRegistrationEntity(UserRegistrationRequest request) {
        UserRegistration userRegistration = new UserRegistration();
        userRegistration.setAge(request.getAge());
        userRegistration.setName(request.getName());
        userRegistration.setEmail(request.getEmail());
        userRegistration.setCountry(request.getCountry());
        userRegistration.setMobilenumber(request.getMobilenumber());
        userRegistration.setPassword(passwordEncoder.encode(request.getPassword()));
        userRegistration.setState(request.getState());
        userRegistration.setUsername(request.getUsername());
        return userRegistration;
    }
    public UserRegistrationResponse convertToUserRegistrationResponse(UserRegistrationRequest request) {
        UserRegistrationResponse userRegistrationResponse = new UserRegistrationResponse();
        userRegistrationResponse.setEmail(request.getEmail());
        userRegistrationResponse.setName(request.getUsername());
        userRegistrationResponse.setMobileno(request.getMobilenumber());
        return userRegistrationResponse;
    }
}
