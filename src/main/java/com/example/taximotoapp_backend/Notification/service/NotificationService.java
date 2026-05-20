package com.example.taximotoapp_backend.Notification.service;

import com.example.taximotoapp_backend.Notification.dto.NotificationDTO;
import com.example.taximotoapp_backend.Notification.model.Notification;
import com.example.taximotoapp_backend.Notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public NotificationDTO createNotification(String title, String message, String type, Long driverId) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setDriverId(driverId);
        notification.setIsRead(false);
        Notification saved = notificationRepository.save(notification);
        NotificationDTO dto = new NotificationDTO(saved);

        // Envoi WebSocket temps réel aux admins abonnés
        try {
            System.out.println("📢 Sending WebSocket notification to /topic/admin/notifications for driver ID: " + driverId);
            messagingTemplate.convertAndSend("/topic/admin/notifications", dto);
        } catch (Exception e) {
            System.err.println("❌ Failed to send WebSocket notification: " + e.getMessage());
        }

        return dto;
    }

    /**
     * Récupère toutes les notifications ordonnées par date décroissante.
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Marque une notification spécifique comme lue.
     */
    @Transactional
    public NotificationDTO markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification introuvable avec l'ID: " + id));
        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);
        return new NotificationDTO(saved);
    }

    /**
     * Marque toutes les notifications non lues comme lues.
     */
    @Transactional
    public void markAllAsRead() {
        List<Notification> unread = notificationRepository.findAll().stream()
                .filter(n -> !n.getIsRead())
                .collect(Collectors.toList());
        for (Notification n : unread) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(unread);
    }
}
