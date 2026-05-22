package com.example.taximotoapp_backend.ai.util;

public class SQLExtractor {

    public static String extract(String text) {

        if (text == null) return "";

        return text
                .replace("```sql", "")
                .replace("```", "")
                .trim();
    }
}