package com.example.taximotoapp_backend.User.repository;


import com.example.taximotoapp_backend.User.model.Chauffeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChauffeurRepository extends JpaRepository<Chauffeur,Long> {
    Optional<Chauffeur> findByEmail(String email);

    @Query(value = """
        SELECT c.* FROM chauffeur c
        WHERE c.available = true
          AND c.status = 'ONLINE'
          
          -- 🔥 pré-filtrage (performance)
          AND c.current_latitude BETWEEN :lat - 0.1 AND :lat + 0.1
          AND c.current_longitude BETWEEN :lon - 0.1 AND :lon + 0.1
          
        ORDER BY (
            6371 * acos(
                LEAST(1, GREATEST(-1,
                    cos(radians(:lat)) *
                    cos(radians(c.current_latitude)) *
                    cos(radians(c.current_longitude) - radians(:lon)) +
                    sin(radians(:lat)) *
                    sin(radians(c.current_latitude))
                ))
            )
        )
        
        LIMIT 3
    """, nativeQuery = true)
    List<Chauffeur> findNearbyDrivers(
            @Param("lat") double lat,
            @Param("lon") double lon
    );

}
