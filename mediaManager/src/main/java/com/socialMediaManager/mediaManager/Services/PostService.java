package com.socialMediaManager.mediaManager.services;

import com.socialMediaManager.mediaManager.dto.TwitterPostRequest;
import com.socialMediaManager.mediaManager.dto.TwitterPostResponse;

public interface PostService {
    TwitterPostResponse post(TwitterPostRequest request);
}
