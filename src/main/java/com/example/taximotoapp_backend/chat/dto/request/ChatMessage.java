package com.example.taximotoapp_backend.chat.dto.request;

import lombok.Data;

@Data
public class ChatMessage {
    private Long chatId;
    private String contenu;
}
