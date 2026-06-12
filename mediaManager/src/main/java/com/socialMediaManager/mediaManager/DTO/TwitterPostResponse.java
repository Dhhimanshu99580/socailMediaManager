package com.socialMediaManager.mediaManager.dto;

import lombok.Data;

@Data
public class TwitterPostResponse {
    private TweetData data;

    @Data
    public static class TweetData {
        private String id;
        private String text;
    }
}
