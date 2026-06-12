package com.socialMediaManager.mediaManager.services;

import com.socialMediaManager.mediaManager.dto.TwitterPostRequest;
import com.socialMediaManager.mediaManager.dto.TwitterPostResponse;
import com.socialMediaManager.mediaManager.entities.PostDetails;
import com.socialMediaManager.mediaManager.repositories.PostDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final Map<String, PlatformPostStrategy> strategies;
    private final PostDetailsRepository postDetailsRepository;

    @Autowired
    public PostServiceImpl(List<PlatformPostStrategy> strategyList, PostDetailsRepository postDetailsRepository) {
        this.postDetailsRepository = postDetailsRepository;
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PlatformPostStrategy::getPlatformName, Function.identity()));
    }

    @Override
    public TwitterPostResponse post(TwitterPostRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String platform = request.getService().toLowerCase();

        PlatformPostStrategy strategy = strategies.get(platform);
        if (strategy == null) {
            throw new RuntimeException("Unsupported platform: " + platform
                    + ". Supported platforms: " + strategies.keySet());
        }

        TwitterPostResponse response = strategy.post(request.getData(), username);

        PostDetails postDetails = new PostDetails();
        postDetails.setUsername(username);
        postDetails.setService(platform);
        postDetails.setData(request.getData());
        postDetails.setEligibleTime(request.getTimeToPost() != null ? request.getTimeToPost() : LocalDateTime.now());
        postDetailsRepository.save(postDetails);

        return response;
    }
}
