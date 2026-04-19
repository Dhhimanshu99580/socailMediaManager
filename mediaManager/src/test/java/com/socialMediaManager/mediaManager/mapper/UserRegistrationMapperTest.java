package com.socialMediaManager.mediaManager.mapper;

import com.socialMediaManager.mediaManager.dto.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.dto.UserRegistrationResponse;
import com.socialMediaManager.mediaManager.entities.UserRegistration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationMapperTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserRegistrationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserRegistrationMapper(passwordEncoder);
    }

    private UserRegistrationRequest buildRequest() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("Himanshu");
        request.setUsername("himanshu99");
        request.setPassword("Secret@123");
        request.setEmail("himanshu@example.com");
        request.setMobilenumber("9958490105");
        request.setCountry("India");
        request.setAge(25);
        request.setState("Delhi");
        return request;
    }

    @Test
    void convertToEntity_mapsAllFieldsCorrectly() {
        when(passwordEncoder.encode("Secret@123")).thenReturn("hashedPassword");
        UserRegistrationRequest request = buildRequest();

        UserRegistration entity = mapper.convertToUserRegistrationEntity(request);

        assertEquals("Himanshu", entity.getName());
        assertEquals("himanshu99", entity.getUsername());
        assertEquals("himanshu@example.com", entity.getEmail());
        assertEquals("9958490105", entity.getMobilenumber());
        assertEquals("India", entity.getCountry());
        assertEquals("Delhi", entity.getState());
        assertEquals(25, entity.getAge());
    }

    @Test
    void convertToEntity_encodesPassword() {
        when(passwordEncoder.encode("Secret@123")).thenReturn("hashedPassword");

        UserRegistration entity = mapper.convertToUserRegistrationEntity(buildRequest());

        assertEquals("hashedPassword", entity.getPassword());
        verify(passwordEncoder).encode("Secret@123");
    }

    @Test
    void convertToResponse_mapsNameEmailMobileno() {
        UserRegistrationRequest request = buildRequest();

        UserRegistrationResponse response = mapper.convertToUserRegistrationResponse(request);

        assertEquals("Himanshu", response.getName());
        assertEquals("himanshu@example.com", response.getEmail());
        assertEquals("9958490105", response.getMobileno());
    }
}
