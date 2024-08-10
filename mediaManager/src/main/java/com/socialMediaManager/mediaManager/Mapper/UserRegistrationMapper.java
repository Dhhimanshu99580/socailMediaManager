package com.socialMediaManager.mediaManager.Mapper;

import com.socialMediaManager.mediaManager.DTO.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.DTO.UserRegistrationResponse;
import com.socialMediaManager.mediaManager.Entities.UserRegistration;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRegistrationMapper {
    public UserRegistration convertToUserRegistrationEntity(UserRegistrationRequest request) {
        UserRegistration userRegistration = new UserRegistration();
        userRegistration.setAge(request.getAge());
        userRegistration.setName(request.getName());
        userRegistration.setEmail(request.getEmail());
        userRegistration.setCountry(request.getCountry());
        userRegistration.setMobilenumber(request.getMobileNo());
        userRegistration.setPassword(request.getPassword());
        userRegistration.setState(request.getState());
        userRegistration.setUsername(request.getUsername());
        return userRegistration;
    }
    public UserRegistrationResponse convertToUserRegistrationResponse(UserRegistrationRequest request) {
        UserRegistrationResponse userRegistrationResponse = new UserRegistrationResponse();
        userRegistrationResponse.setEmail(request.getEmail());
        userRegistrationResponse.setName(request.getUsername());
        userRegistrationResponse.setMobileno(request.getMobileNo());
        return userRegistrationResponse;
    }
}
