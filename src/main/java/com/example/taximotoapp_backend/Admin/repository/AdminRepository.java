package com.example.taximotoapp_backend.Admin.repository;

import com.example.taximotoapp_backend.Admin.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);
}
