package com.socialMediaManager.mediaManager.services;

import com.socialMediaManager.mediaManager.dto.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.dto.UserRegistrationResponse;

public interface UserService {
    UserRegistrationResponse register(UserRegistrationRequest request);
}
