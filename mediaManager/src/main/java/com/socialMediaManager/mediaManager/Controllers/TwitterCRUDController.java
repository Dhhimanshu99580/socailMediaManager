package com.socialMediaManager.mediaManager.controllers;

import com.socialMediaManager.mediaManager.dto.TwitterPostRequest;
import com.socialMediaManager.mediaManager.dto.TwitterPostResponse;
import com.socialMediaManager.mediaManager.services.TwitterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mediaManager/v1")
public class TwitterCRUDController {

    @Autowired
    private TwitterService twitterService;

    @PostMapping("/post")
    public ResponseEntity<TwitterPostResponse> postOnTwitter(@Valid @RequestBody TwitterPostRequest request) {
        TwitterPostResponse response = twitterService.postOnTwitter(request);
        return ResponseEntity.ok(response);
    }
}
