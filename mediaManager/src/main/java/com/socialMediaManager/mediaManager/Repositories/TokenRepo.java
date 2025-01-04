package com.socialMediaManager.mediaManager.repositories;

import com.socialMediaManager.mediaManager.entities.UserTokens;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepo extends JpaRepository<UserTokens,Long> {

}
