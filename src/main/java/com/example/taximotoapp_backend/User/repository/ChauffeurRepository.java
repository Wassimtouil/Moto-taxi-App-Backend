package com.example.taximotoapp_backend.User.repository;


import com.example.taximotoapp_backend.User.model.Chauffeur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChauffeurRepository extends JpaRepository<Chauffeur,Long> {
    Optional<Chauffeur> findByEmail(String email);
}
