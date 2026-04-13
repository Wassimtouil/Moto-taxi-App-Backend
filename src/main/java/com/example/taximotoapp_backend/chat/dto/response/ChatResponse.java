package com.example.taximotoapp_backend.chat.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatResponse {
    private Long chatId;
    private Long trajetId;
    private LocalDateTime createdAt;
}
