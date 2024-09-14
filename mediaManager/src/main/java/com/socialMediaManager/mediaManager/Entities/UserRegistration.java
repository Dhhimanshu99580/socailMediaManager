package com.socialMediaManager.mediaManager.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Entity
@Table(name = "user-registration")
@Data
public class UserRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false,length = 30)
    private String name;
    @Column(unique = true,nullable = false,length = 15)
    private String username;
    @Size(min = 8,max=15)
    @Column(nullable = false)
    private String password;
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", flags = Pattern.Flag.CASE_INSENSITIVE)
    @Column(nullable = false,unique = true)
    private String email;
    @Pattern(regexp = "\\d{10}")
    @Column(nullable = false,unique=true)
    private String mobilenumber;
    @Column(nullable = false)
    private int age;
    @Column(nullable = false)
    private String country;
    private String state;
}
