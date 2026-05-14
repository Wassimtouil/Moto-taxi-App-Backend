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
    @Query("SELECT t FROM Trajet t WHERE t.chauffeur.id = :chauffeurId AND (t.status = 'Accepted' OR t.status = 'Arrived' OR t.status = 'Started') ORDER BY t.id DESC")
    List<Trajet> findActiveTrajetByChauffeurId(@Param("chauffeurId") Long chauffeurId);

    @Query("SELECT t FROM Trajet t WHERE t.client.id = :clientId AND (t.status = 'Accepted' OR t.status = 'Arrived' OR t.status = 'Started') ORDER BY t.id DESC")
    List<Trajet> findActiveTrajetByClientId(@Param("clientId") Long clientId);


    @Query(value = """
    SELECT t.*
    FROM trajet t
    JOIN trajet_location tl ON tl.trajet_id = t.id
    WHERE t.status = 'Created'
      AND t.chauffeur_id IS NULL
      AND (t.preferred_driver_id IS NULL OR t.preferred_driver_id = :chauffeurId)
      AND tl.pickup_latitude BETWEEN :lat - (:radius / 111) AND :lat + (:radius / 111)
      AND tl.pickup_longitude BETWEEN :lon - (:radius / (111 * cos(radians(:lat))))
                                 AND :lon + (:radius / (111 * cos(radians(:lat))))
    ORDER BY (
        6371 * acos(
            LEAST(1, GREATEST(-1,
                cos(radians(:lat)) *
                cos(radians(tl.pickup_latitude)) *
                cos(radians(tl.pickup_longitude) - radians(:lon)) +
                sin(radians(:lat)) *
                sin(radians(tl.pickup_latitude))
            ))
        )
    )
""", nativeQuery = true)
    List<Trajet> findNearbyAvailableTrajets(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") double radius,
            @Param("chauffeurId") Long chauffeurId
    );

    List<Trajet> findByChauffeurIdAndStatus(Long chauffeurId, TripStatus status);

    List<Trajet> findByClientIdOrderByRequestedAtDesc(Long clientId);
    List<Trajet> findByChauffeurIdOrderByRequestedAtDesc(Long chauffeurId);

    @Query("SELECT t FROM Trajet t WHERE t.status = 'Scheduled' AND t.scheduledAt <= :limitTime")
    List<Trajet> findUpcomingScheduledTrajets(@Param("limitTime") java.time.LocalDateTime limitTime);

    @Query("SELECT t FROM Trajet t WHERE t.status = 'Created' AND t.chauffeur IS NULL AND t.scheduledAt IS NOT NULL AND t.scheduledAt < :now")
    List<Trajet> findExpiredScheduledTrajets(@Param("now") java.time.LocalDateTime now);

    // Dashboard methods
    long countByRequestedAtAfter(java.time.LocalDateTime startOfDay);

    @Query("SELECT t FROM Trajet t ORDER BY t.requestedAt DESC")
    List<Trajet> findRecentTrajets(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT CAST(t.requestedAt AS date), COUNT(t) FROM Trajet t WHERE t.requestedAt >= :since GROUP BY CAST(t.requestedAt AS date) ORDER BY CAST(t.requestedAt AS date) ASC")
    List<Object[]> countTripsByDate(@Param("since") java.time.LocalDateTime since);



    @Query(value = "SELECT MONTHNAME(requested_at) as month, COUNT(*) as count FROM trajet WHERE requested_at >= :since GROUP BY MONTH(requested_at), MONTHNAME(requested_at) ORDER BY MONTH(requested_at) ASC", nativeQuery = true)
    List<Object[]> countTripsByMonth(@Param("since") java.time.LocalDateTime since);

    @Query(value = "SELECT HOUR(requested_at) as hour, COUNT(*) as count FROM trajet GROUP BY HOUR(requested_at) ORDER BY HOUR(requested_at) ASC", nativeQuery = true)
    List<Object[]> countTripsByHour();

    @Query(value = "SELECT DAYNAME(requested_at) as day, COUNT(*) as count FROM trajet GROUP BY DAYOFWEEK(requested_at), DAYNAME(requested_at) ORDER BY DAYOFWEEK(requested_at) ASC", nativeQuery = true)
    List<Object[]> countTripsByDayOfWeek();

    @Query(value = """
        SELECT tl.pickup_address as zone,
               AVG(tl.pickup_latitude) as lat,
               AVG(tl.pickup_longitude) as lng,
               COUNT(*) as count
        FROM trajet_location tl
        JOIN trajet t ON t.id = tl.trajet_id
        WHERE tl.pickup_address IS NOT NULL AND tl.pickup_address != ''
        GROUP BY ROUND(tl.pickup_latitude, 3), ROUND(tl.pickup_longitude, 3), tl.pickup_address
        ORDER BY count DESC
    """, nativeQuery = true)
    List<Object[]> findTopPickupZones();
}
