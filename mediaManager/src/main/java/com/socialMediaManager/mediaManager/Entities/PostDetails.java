package com.socialMediaManager.mediaManager.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Table(name= "postDetails")
@Data
public class PostDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true,nullable = false,length = 30)
    private String username;
    @Column(nullable = false,length=20)
    private String service;
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime addedOn;
    @Column(updatable = false,nullable = false)
    private LocalDateTime eligibleTime;
    @Column(nullable = false,length=200)
    private String data;

}
