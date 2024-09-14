package com.socialMediaManager.mediaManager.controllers;

import com.socialMediaManager.mediaManager.dto.AuthResponse;
import com.socialMediaManager.mediaManager.dto.UserLoginRequest;
import com.socialMediaManager.mediaManager.dto.UserLoginResponse;
import com.socialMediaManager.mediaManager.dto.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.dto.UserRegistrationResponse;
import com.socialMediaManager.mediaManager.services.CustomUserDetailsService;
import com.socialMediaManager.mediaManager.services.TwitterServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.socialMediaManager.mediaManager.utility.JwtTokenProvider;

@RestController
public class UserRegistrationController  {
    @Autowired
    private TwitterServiceImpl twitterService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @PostMapping("/user-registartion")
    public UserRegistrationResponse saveRegistartionDetails(@RequestBody @Valid  UserRegistrationRequest request) {
        return twitterService.processAndSaveUserRegistrationDetails(request);
    }
    @PostMapping("/user-login")
    public ResponseEntity<?> userLogin(@RequestBody @Valid UserLoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),
                    request.getPassword()));

        } catch(AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials");
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String jwt = jwtTokenProvider.generateToken(userDetails.getUsername());
        return ResponseEntity.ok(jwt);
    }
}
