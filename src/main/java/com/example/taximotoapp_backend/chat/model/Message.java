package com.example.taximotoapp_backend.chat.model;


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

    public Chat getChat() {
        return chat;
    }

    public SenderType getSenderType() {
        return senderType;
    }

    public Long getId() {
        return id;
    }

    public String getContenu() {
        return contenu;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public void setSenderType(SenderType senderType) {
        this.senderType = senderType;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
