package com.example.taximotoapp_backend.chat.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private Long id;
    private Long chatId;
    private String contenu;
    private String sender;
    private LocalDateTime sentAt;
}
