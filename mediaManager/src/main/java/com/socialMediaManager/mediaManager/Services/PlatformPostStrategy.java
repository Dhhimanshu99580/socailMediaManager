package com.socialMediaManager.mediaManager.services;

import com.socialMediaManager.mediaManager.dto.TwitterPostResponse;

public interface PlatformPostStrategy {
    TwitterPostResponse post(String text, String username);
    String getPlatformName();
}
