package com.socialMediaManager.mediaManager.services;

import com.socialMediaManager.mediaManager.dto.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.dto.UserRegistrationResponse;
import com.socialMediaManager.mediaManager.entities.UserRegistration;
import com.socialMediaManager.mediaManager.exceptions.userAlreadyExistsException;
import com.socialMediaManager.mediaManager.mapper.UserRegistrationMapper;
import com.socialMediaManager.mediaManager.repositories.UserRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRegistrationRepository userRegistrationRepository;
    private final UserRegistrationMapper userRegistrationMapper;

    @Autowired
    public UserServiceImpl(UserRegistrationRepository userRegistrationRepository,
                           UserRegistrationMapper userRegistrationMapper) {
        this.userRegistrationRepository = userRegistrationRepository;
        this.userRegistrationMapper = userRegistrationMapper;
    }

    @Override
    public UserRegistrationResponse register(UserRegistrationRequest request) {
        Optional<UserRegistration> existing = userRegistrationRepository.findByEmailAndMobilenumber(
                request.getEmail(), request.getMobilenumber());
        if (existing.isPresent()) {
            throw new userAlreadyExistsException("User with email " + request.getEmail()
                    + " and mobile number " + request.getMobilenumber() + " already exists.");
        }
        userRegistrationRepository.save(userRegistrationMapper.convertToUserRegistrationEntity(request));
        return userRegistrationMapper.convertToUserRegistrationResponse(request);
    }
}
