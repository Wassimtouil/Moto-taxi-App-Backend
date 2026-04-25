package com.example.taximotoapp_backend.Evaluation.Repository;

import com.example.taximotoapp_backend.Evaluation.model.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation,Long> {
    Optional<Evaluation> findByTrajetId(Long trajetId);
    List<Evaluation> findByChauffeurId(Long chauffeurId);

    @Query("select avg(e.note) from Evaluation e where e.chauffeur.id= :id")
    Double getMoyenne(@Param("id") Long id);
}
