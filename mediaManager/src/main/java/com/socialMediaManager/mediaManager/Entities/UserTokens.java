package com.socialMediaManager.mediaManager.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Table(name = "userTokens")
@Data
public class UserTokens {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true,nullable = false,length = 30)
    private String username;
    @Column(unique = true,nullable = false,length=50)
    private String token;
    @Column(unique = true,nullable = true,length = 50)
    private String refreshToken;
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime addedOn;
    @UpdateTimestamp
    private LocalDateTime updatedOn;

}
