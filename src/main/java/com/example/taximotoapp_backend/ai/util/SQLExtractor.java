package com.example.taximotoapp_backend.ai.util;

public class SQLExtractor {

    public static String extract(String text) {

        if (text == null) return "";

        // 1. remove markdown
        text = text.replace("```sql", "")
                .replace("```", "")
                .trim();

        // 2. extract first SELECT safely
        int select = text.toUpperCase().indexOf("SELECT");

        if (select == -1) {
            throw new RuntimeException("No SELECT found in AI response");
        }

        text = text.substring(select);

        // 3. cut garbage after SQL (optional safety)
        text = text.split("(?i)\\n\\n")[0];

        return text.trim();
    }
}