package com.socialMediaManager.mediaManager.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_details")
@Data
public class PostDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(nullable = false, length = 20)
    private String service;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime addedOn;

    @Column(updatable = false, nullable = false)
    private LocalDateTime eligibleTime;

    @Column(nullable = false, length = 280)
    private String data;
}
