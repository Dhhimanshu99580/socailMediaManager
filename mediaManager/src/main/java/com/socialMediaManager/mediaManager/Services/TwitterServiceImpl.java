package com.socialMediaManager.mediaManager.Services;

import com.socialMediaManager.mediaManager.DTO.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.DTO.UserRegistrationResponse;
import com.socialMediaManager.mediaManager.Entities.UserRegistration;
import com.socialMediaManager.mediaManager.Exceptions.userAlreadyExistsException;
import com.socialMediaManager.mediaManager.Mapper.UserRegistrationMapper;
import com.socialMediaManager.mediaManager.Repositories.TwitterServiceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TwitterServiceImpl implements TwitterService {

    private final TwitterServiceRepo twitterServiceRepo;
    private final UserRegistrationMapper userRegistrationMapper;
    @Autowired
    public TwitterServiceImpl(TwitterServiceRepo twitterServiceRepo
            ,UserRegistrationMapper userRegistrationMapper) {
        this.twitterServiceRepo = twitterServiceRepo;
        this.userRegistrationMapper = userRegistrationMapper;
    }
    @Override
    public UserRegistrationResponse processAndSaveUserRegistrationDetails(UserRegistrationRequest request) {
        Optional<UserRegistration> user = twitterServiceRepo.findByEmailAndMobileno(request.getEmail(),request.getMobilenumber());
        if(user.isPresent()) {
            throw new userAlreadyExistsException("User with email " + request.getEmail() +
                    " and mobile number " + request.getMobilenumber() + " already exists.");
        }
        twitterServiceRepo.save(userRegistrationMapper.convertToUserRegistrationEntity(request));
        return userRegistrationMapper.convertToUserRegistrationResponse(request);
    }
}
