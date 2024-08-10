package com.socialMediaManager.mediaManager.Services;

import com.socialMediaManager.mediaManager.DTO.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.DTO.UserRegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;

public interface TwitterService {
    public UserRegistrationResponse processAndSaveUserRegistrationDetails(UserRegistrationRequest request);
}
