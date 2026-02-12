package com.example.taximotoapp_backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String email;
    @Column
    private String password; // null si OAuth
    @Column(name = "firebase_uid", unique = true)
    private String firebaseUid; // UID Firebase
    @Column(name = "full_name")
    private String fullName;
    @Column(name = "photo_url")
    private String photoUrl;
    @Enumerated(EnumType.STRING)
    private Role role; // CLIENT, CHAUFFEUR

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

