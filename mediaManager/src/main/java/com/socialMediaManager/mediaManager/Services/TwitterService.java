package com.socialMediaManager.mediaManager.services;

import com.socialMediaManager.mediaManager.dto.UserLoginRequest;
import com.socialMediaManager.mediaManager.dto.UserLoginResponse;
import com.socialMediaManager.mediaManager.dto.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.dto.UserRegistrationResponse;

public interface TwitterService {
    public UserRegistrationResponse processAndSaveUserRegistrationDetails(UserRegistrationRequest request);

    public String getAuthorizationUrl () throws Exception;
    //public UserLoginResponse userLogin(UserLoginRequest request);
}
