package com.example.taximotoapp_backend.ai.service;

import org.springframework.stereotype.Service;

@Service
public class ResponseFormatterService {

    public String format(String question, Object data) {

        return "Based on your request, here are the results: " + data.toString();
    }
}