package com.example.taximotoapp_backend.location.repository;

import com.example.taximotoapp_backend.location.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
