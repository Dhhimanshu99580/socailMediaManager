package com.socialMediaManager.mediaManager.repositories;

import com.socialMediaManager.mediaManager.entities.PostDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostDetailsRepository extends JpaRepository<PostDetails, Long> {
    List<PostDetails> findByUsername(String username);
}
