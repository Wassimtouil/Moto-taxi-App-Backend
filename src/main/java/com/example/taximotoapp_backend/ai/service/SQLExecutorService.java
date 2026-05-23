package com.example.taximotoapp_backend.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SQLExecutorService {

    private final JdbcTemplate jdbcTemplate;

    public Object execute(String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}