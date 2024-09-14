package com.socialMediaManager.mediaManager.dto;

import lombok.Data;

@Data
public class UserLoginResponse {
    private String token;
    private String username;
}
