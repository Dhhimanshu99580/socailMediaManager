package com.socialMediaManager.mediaManager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRegistrationRequest {
    @NotNull private String name;
    @NotNull private String username;
    @NotNull private String password;
    @NotNull private String email;
    @NotNull private String mobilenumber;
    @NotNull private String country;
    @NotNull private int age;
    private String state;
}
