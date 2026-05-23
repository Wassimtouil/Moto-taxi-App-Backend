package com.example.taximotoapp_backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatResponse {
    private String naturalResponse;
    private String sql;
    private Object data;
    private boolean fromCache;
}

