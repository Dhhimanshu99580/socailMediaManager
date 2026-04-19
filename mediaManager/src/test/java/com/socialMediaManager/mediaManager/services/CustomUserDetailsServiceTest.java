package com.socialMediaManager.mediaManager.services;

import com.socialMediaManager.mediaManager.entities.UserRegistration;
import com.socialMediaManager.mediaManager.repositories.TwitterServiceRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private TwitterServiceRepo userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private UserRegistration buildUser(String username, String password) {
        UserRegistration user = new UserRegistration();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    @Test
    void loadUserByUsername_existingUser_returnsUserDetails() {
        UserRegistration user = buildUser("himanshu", "hashedPassword");
        when(userRepository.findByUsername("himanshu")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("himanshu");

        assertNotNull(userDetails);
        assertEquals("himanshu", userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_returnsCorrectPassword() {
        UserRegistration user = buildUser("himanshu", "hashedPassword");
        when(userRepository.findByUsername("himanshu")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("himanshu");

        assertEquals("hashedPassword", userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_userNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknown"));
    }

    @Test
    void loadUserByUsername_queriesRepositoryWithCorrectUsername() {
        UserRegistration user = buildUser("himanshu", "pass");
        when(userRepository.findByUsername("himanshu")).thenReturn(Optional.of(user));

        userDetailsService.loadUserByUsername("himanshu");

        verify(userRepository).findByUsername("himanshu");
    }
}
