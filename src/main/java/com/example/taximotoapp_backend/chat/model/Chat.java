package com.example.taximotoapp_backend.chat.model;


import com.example.taximotoapp_backend.trajet.model.Trajet;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "trajet_id", nullable = false, unique = true)
    private Trajet trajet;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setTrajet(Trajet trajet) {
        this.trajet = trajet;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Trajet getTrajet() {
        return trajet;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}