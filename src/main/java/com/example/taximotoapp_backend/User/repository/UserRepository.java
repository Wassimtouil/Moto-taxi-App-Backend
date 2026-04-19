package com.example.taximotoapp_backend.User.repository;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.model.enumClass.Role;
import com.example.taximotoapp_backend.reclamation.model.Reclamation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByFirebaseUid(String firebaseUid);
    List<User> findByRole(Role role);
    Boolean existsByEmail(String email);
}
