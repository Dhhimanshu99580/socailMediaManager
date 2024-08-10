package com.socialMediaManager.mediaManager.Services;

import com.socialMediaManager.mediaManager.DTO.UserRegistrationRequest;
import com.socialMediaManager.mediaManager.DTO.UserRegistrationResponse;
import com.socialMediaManager.mediaManager.Entities.UserRegistration;
import com.socialMediaManager.mediaManager.Exceptions.userAlreadyExistsException;
import com.socialMediaManager.mediaManager.Mapper.UserRegistrationMapper;
import com.socialMediaManager.mediaManager.Repositories.TwitterServiceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        UserRegistration user = twitterServiceRepo.findByEmailAndMobileno(request.getEmail(),request.getMobileNo()).
                                orElseThrow(()-> new userAlreadyExistsException("User with email " + request.getEmail() +
                                        " and mobile number " + request.getMobileNo() + " already exists."));
        twitterServiceRepo.save(userRegistrationMapper.convertToUserRegistrationEntity(request));
        return userRegistrationMapper.convertToUserRegistrationResponse(request);
    }
}
