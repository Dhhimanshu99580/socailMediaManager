package com.socialMediaManager.mediaManager.Repositories;

import com.socialMediaManager.mediaManager.Entities.UserRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TwitterServiceRepo extends JpaRepository<UserRegistration,Long> {
    Optional<UserRegistration> findByEmailAndMobileno(String email, String mobileno);
}
