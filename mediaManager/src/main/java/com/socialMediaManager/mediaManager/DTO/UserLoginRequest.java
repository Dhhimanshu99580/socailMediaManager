package com.socialMediaManager.mediaManager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserLoginRequest {
    @NotNull private String username;
    @NotNull private String password;
}
