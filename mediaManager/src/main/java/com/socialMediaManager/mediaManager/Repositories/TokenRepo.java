package com.socialMediaManager.mediaManager.repositories;

import com.socialMediaManager.mediaManager.entities.UserTokens;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepo extends JpaRepository<UserTokens, Long> {
    Optional<UserTokens> findByUsername(String username);
}
