package com.example.taximotoapp_backend.reclamation.repository;

import com.example.taximotoapp_backend.reclamation.model.Reclamation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReclamationRepository extends JpaRepository<Reclamation,Long> {
}
