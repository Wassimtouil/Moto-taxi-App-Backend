package com.example.taximotoapp_backend.trajet.repository;

import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrajetRepository extends JpaRepository<Trajet,Long> {
    Optional<Trajet> findById(Long id);
    List<Trajet> findByStatus(TripStatus status);
    @Query("SELECT t FROM Trajet t WHERE t.chauffeur.id = :chauffeurId AND (t.status = 'Accepted' OR t.status = 'Started') ORDER BY t.id DESC")
    List<Trajet> findActiveTrajetByChauffeurId(@Param("chauffeurId") Long chauffeurId);

    @Query("SELECT t FROM Trajet t WHERE t.client.id = :clientId AND (t.status = 'Accepted' OR t.status = 'Started') ORDER BY t.id DESC")
    List<Trajet> findActiveTrajetByClientId(@Param("clientId") Long clientId);


    @Query(value = """
    SELECT t.*
    FROM trajet t
    WHERE t.status = 'Created'
      AND t.chauffeur_id IS NULL
      AND t.pickup_latitude BETWEEN :lat - (:radius / 111) AND :lat + (:radius / 111)
      AND t.pickup_longitude BETWEEN :lon - (:radius / (111 * cos(radians(:lat))))
                                 AND :lon + (:radius / (111 * cos(radians(:lat))))
    ORDER BY (
        6371 * acos(
            LEAST(1, GREATEST(-1,
                cos(radians(:lat)) *
                cos(radians(t.pickup_latitude)) *
                cos(radians(t.pickup_longitude) - radians(:lon)) +
                sin(radians(:lat)) *
                sin(radians(t.pickup_latitude))
            ))
        )
    )
""", nativeQuery = true)
    List<Trajet> findNearbyAvailableTrajets(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") double radius
    );

}
