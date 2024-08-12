package com.socialMediaManager.mediaManager.DTO;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserRegistrationRequest {
    private @NotNull String name;
    private @NotNull String username;
    private @NotNull String password;
    private @NotNull String email;
    private @NotNull String mobilenumber;
    private @NotNull String country;
    private @NotNull int age;
    private String state;
}
