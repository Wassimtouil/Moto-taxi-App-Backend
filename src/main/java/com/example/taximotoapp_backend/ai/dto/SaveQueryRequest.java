package com.example.taximotoapp_backend.ai.dto;

import lombok.Data;

@Data
public class SaveQueryRequest {
    private String question;
    private String sql;
}
