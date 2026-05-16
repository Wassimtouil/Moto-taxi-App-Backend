package com.example.taximotoapp_backend.reclamation.repository;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.model.enumClass.ReclamationStatus;
import com.example.taximotoapp_backend.reclamation.model.Reclamation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReclamationRepository extends JpaRepository<Reclamation,Long> {
    List<Reclamation> findByUser(User user);

    long countByReclamationStatus(ReclamationStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT r.objet, COUNT(r) FROM Reclamation r GROUP BY r.objet ORDER BY COUNT(r) DESC")
    List<Object[]> countByObjet();
}
