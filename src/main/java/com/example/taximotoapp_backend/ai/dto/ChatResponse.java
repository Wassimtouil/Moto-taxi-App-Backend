package com.example.taximotoapp_backend.ai.dto;

import lombok.Data;

@Data
public class ChatResponse {
    private String naturalResponse;
    private String sql;
    private Object data;
}
