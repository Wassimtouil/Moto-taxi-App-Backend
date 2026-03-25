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
    SELECT c.* ,u.*
    FROM chauffeur c
    JOIN user u ON c.user_id = u.id
    WHERE c.availability = true
      AND c.current_latitude BETWEEN :lat - (:radius / 111) AND :lat + (:radius / 111)
      AND c.current_longitude BETWEEN :lon - (:radius / (111 * cos(radians(:lat)))) 
                                 AND :lon + (:radius / (111 * cos(radians(:lat))))
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
            @Param("lon") double lon,
            @Param("radius") double radius
    );



}
