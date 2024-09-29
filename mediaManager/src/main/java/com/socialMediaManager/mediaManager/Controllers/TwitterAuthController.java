package com.socialMediaManager.mediaManager.controllers;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.socialMediaManager.mediaManager.services.TwitterService;
import com.socialMediaManager.mediaManager.utility.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/twitter")
public class TwitterAuthController {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private TwitterService twitterService;
    @GetMapping("/authorize")
    public ResponseEntity<String> startTwitterAuthorization(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if(authorizationHeader==null||!authorizationHeader.startsWith("Bearer")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or Invalid Token");
        }
        String token = authorizationHeader.substring(7);
        if(!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
        //Here we will initiate twitter Oauth flow.
        try{
            String twitterAuthUrl = twitterService.getAuthorizationUrl();
            return ResponseEntity.ok(twitterAuthUrl);
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Twiiter Authorization Failed");
        }
    }
    @GetMapping("/callback")
    public ResponseEntity<String> twitterCallback(@RequestParam("code") String code,
                                                  @RequestParam("state")String state) {
        if (!state.equals("RANDOM_STATE_STRING")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid state"); }
        try{
            OAuth2AccessToken accessToken = twitterService.getAccessToken()
            OAuth1AccessToken oAuth1AccessToken  = service.getAccessToken(oAuth1RequestToken,oauthVerifier);
            String token = oAuth1AccessToken.getToken();
            String tokenSecret = oAuth1AccessToken.getTokenSecret();
            //Need to store these token and tokenSecret with regard to each user
            return ResponseEntity.ok("Twitter user is authenticated successfully");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Twitter authorization failed for the user");
        }

    }
}
