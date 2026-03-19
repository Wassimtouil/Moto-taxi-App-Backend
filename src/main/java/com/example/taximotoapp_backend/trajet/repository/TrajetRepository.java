package com.example.taximotoapp_backend.trajet.repository;

import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrajetRepository extends JpaRepository<Trajet,Long> {
    Optional<Trajet> findById(Long id);
    List<Trajet> findByStatus(TripStatus status);
}
