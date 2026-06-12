package com.socialMediaManager.mediaManager.repositories;

import com.socialMediaManager.mediaManager.entities.UserRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRegistrationRepository extends JpaRepository<UserRegistration, Long> {
    Optional<UserRegistration> findByEmailAndMobilenumber(String email, String mobileno);
    Optional<UserRegistration> findByMobilenumber(String mobileno);
    Optional<UserRegistration> findByUsername(String username);
}
