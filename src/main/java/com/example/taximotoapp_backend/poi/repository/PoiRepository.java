package com.example.taximotoapp_backend.poi.repository;

import com.example.taximotoapp_backend.poi.model.Poi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PoiRepository extends JpaRepository<Poi,Long> {
    @Query("""
        SELECT p FROM Poi p
        LEFT JOIN PoiSynonym s ON s.poi = p
        WHERE LOWER(p.name) = LOWER(:text)
        OR LOWER(s.synonym) = LOWER(:text)
    """)
    List<Poi> search(@Param("text") String text);
}
