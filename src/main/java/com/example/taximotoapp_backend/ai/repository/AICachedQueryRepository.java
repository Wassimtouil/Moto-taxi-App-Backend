package com.example.taximotoapp_backend.ai.repository;

import com.example.taximotoapp_backend.ai.model.AICachedQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AICachedQueryRepository extends JpaRepository<AICachedQuery, Long> {
    Optional<AICachedQuery> findByQuestion(String question);
}