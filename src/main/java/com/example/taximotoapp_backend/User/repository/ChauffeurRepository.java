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
        SELECT * FROM chauffeur c
        HAVING (
            6371 * acos(
                cos(radians(:lat)) *
                cos(radians(c.current_latitude)) *
                cos(radians(c.current_longitude) - radians(:lon)) +
                sin(radians(:lat)) *
                sin(radians(c.current_latitude))
            )
        ) <= :radius
        ORDER BY distance
        LIMIT 3
    """, nativeQuery = true)
    List<Chauffeur> findNearbyDrivers(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") double radius
    );
}
