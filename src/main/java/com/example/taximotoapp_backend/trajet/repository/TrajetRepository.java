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


    @Query("SELECT t FROM Trajet t WHERE t.chauffeur.id = :chauffeurId AND t.status = 'Accepted'")
    Optional<Trajet> findActiveTrajetByChauffeurId(@Param("chauffeurId") Long chauffeurId);

}
