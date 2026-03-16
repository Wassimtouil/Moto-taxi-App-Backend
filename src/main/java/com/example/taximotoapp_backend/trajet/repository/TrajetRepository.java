package com.example.taximotoapp_backend.trajet.repository;

import com.example.taximotoapp_backend.trajet.model.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrajetRepository extends JpaRepository<Trajet,Long> {
}
