package com.socialMediaManager.mediaManager.dto;

import jakarta.validation.constraints.NotNull;

public class TwitterPostRequest {
    @NotNull
    private String data;
}
