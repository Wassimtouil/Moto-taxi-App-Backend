package com.example.taximotoapp_backend.model;

import jakarta.persistence.*;

@Entity
@PrimaryKeyJoinColumn(name = "user_id")
public class Chauffeur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


}
