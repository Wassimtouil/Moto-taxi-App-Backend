package com.example.taximotoapp_backend.User.repository;

import com.example.taximotoapp_backend.User.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client,Long> {
}
