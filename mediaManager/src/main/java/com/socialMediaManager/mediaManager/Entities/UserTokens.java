package com.socialMediaManager.mediaManager.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_tokens")
@Data
public class UserTokens {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String username;

    @Column(unique = true, nullable = false, length = 2048)
    private String token;

    @Column(unique = true, length = 2048)
    private String refreshToken;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime addedOn;

    @UpdateTimestamp
    private LocalDateTime updatedOn;
}
