package com.example.taximotoapp_backend.model;

import com.example.taximotoapp_backend.model.enumClass.SenderType;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SenderType senderType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();
}
