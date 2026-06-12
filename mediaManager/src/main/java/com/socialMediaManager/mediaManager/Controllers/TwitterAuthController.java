package com.socialMediaManager.mediaManager.controllers;

import com.socialMediaManager.mediaManager.services.TwitterAuthService;
import com.socialMediaManager.mediaManager.utility.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/twitter")
public class TwitterAuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TwitterAuthService twitterAuthService;

    @GetMapping("/authorize")
    public ResponseEntity<String> startTwitterAuthorization(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }
        String token = authorizationHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
        String username = jwtTokenProvider.getUsernameFromToken(token);
        try {
            String twitterAuthUrl = twitterAuthService.getAuthorizationUrl(username);
            return ResponseEntity.ok(twitterAuthUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Twitter authorization failed");
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<String> twitterCallback(@RequestParam("code") String code,
                                                   @RequestParam("state") String state) {
        try {
            twitterAuthService.getAccessToken(code, state);
            return ResponseEntity.ok("Twitter connected successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Twitter authorization failed: " + e.getMessage());
        }
    }
}
