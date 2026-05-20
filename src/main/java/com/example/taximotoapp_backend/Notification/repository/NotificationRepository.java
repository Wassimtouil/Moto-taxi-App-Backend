package com.example.taximotoapp_backend.Notification.repository;

import com.example.taximotoapp_backend.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc();
    long countByIsReadFalse();
}
