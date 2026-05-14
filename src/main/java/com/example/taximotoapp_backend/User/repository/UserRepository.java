package com.example.taximotoapp_backend.User.repository;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.model.enumClass.Role;
import com.example.taximotoapp_backend.reclamation.model.Reclamation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByFirebaseUid(String firebaseUid);
    List<User> findByRole(Role role);
    Boolean existsByEmail(String email);

    Page<User> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);

    Page<User> findByRole(Role role, Pageable pageable);

    @Query("SELECT CAST(u.createdAt AS date), COUNT(u) " +
            "FROM User u WHERE u.role = :role " +
            "GROUP BY CAST(u.createdAt AS date) " +
            "ORDER BY CAST(u.createdAt AS date) ASC")
    List<Object[]> countRegistrationsByDate(@Param("role") Role role);

    @Query("SELECT u.gender, COUNT(u) FROM User u WHERE u.role = :role GROUP BY u.gender")
    List<Object[]> countByRoleAndGender(@Param("role") Role role);

    @Query("SELECT " +
            "SUM(CASE WHEN u.age BETWEEN 16 AND 25 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN u.age BETWEEN 26 AND 35 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN u.age BETWEEN 36 AND 45 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN u.age BETWEEN 46 AND 60 THEN 1 ELSE 0 END) " +
            "FROM User u")
    List<Object[]> countUsersByAgeGroups();
}
