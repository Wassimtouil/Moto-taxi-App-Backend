package com.example.taximotoapp_backend.chat.repository;

import com.example.taximotoapp_backend.chat.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat,Long> {
    Optional<Chat> findByTrajetId(Long trajetId);
}
