package com.example.taximotoapp_backend.ai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_cached_query")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AICachedQuery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", unique = true, nullable = false)
    private String question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String sqlQuery;

    private LocalDateTime createdAt = LocalDateTime.now();
}
