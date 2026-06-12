package com.socialMediaManager.mediaManager.services;

import com.socialMediaManager.mediaManager.dto.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.dto.UserRegistrationResponse;
import com.socialMediaManager.mediaManager.entities.UserRegistration;
import com.socialMediaManager.mediaManager.exceptions.userAlreadyExistsException;
import com.socialMediaManager.mediaManager.mapper.UserRegistrationMapper;
import com.socialMediaManager.mediaManager.repositories.UserRegistrationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRegistrationRepository userRegistrationRepository;
    @Mock private UserRegistrationMapper userRegistrationMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void register_newUser_savesAndReturnsResponse() {
        UserRegistrationRequest request = buildRequest();
        UserRegistration entity = new UserRegistration();
        UserRegistrationResponse expectedResponse = new UserRegistrationResponse();

        when(userRegistrationRepository.findByEmailAndMobilenumber(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(userRegistrationMapper.convertToUserRegistrationEntity(request)).thenReturn(entity);
        when(userRegistrationMapper.convertToUserRegistrationResponse(request)).thenReturn(expectedResponse);

        UserRegistrationResponse result = userService.register(request);

        assertNotNull(result);
        verify(userRegistrationRepository).save(entity);
    }

    @Test
    void register_existingUser_throwsUserAlreadyExistsException() {
        UserRegistrationRequest request = buildRequest();
        when(userRegistrationRepository.findByEmailAndMobilenumber(anyString(), anyString()))
                .thenReturn(Optional.of(new UserRegistration()));

        assertThrows(userAlreadyExistsException.class, () -> userService.register(request));
        verify(userRegistrationRepository, never()).save(any());
    }

    private UserRegistrationRequest buildRequest() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("Himanshu");
        request.setUsername("himanshu99");
        request.setPassword("Secret@123");
        request.setEmail("him@example.com");
        request.setMobilenumber("9958490105");
        request.setCountry("India");
        request.setAge(25);
        return request;
    }
}
