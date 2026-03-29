package com.example.taximotoapp_backend.websocket.service;

import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
@EnableAsync
public class TimeoutService {

    private final TrajetRepository trajetRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    public void handleTimeout(Long trajetId) {

        try {
            Thread.sleep(15000); // 15 sec

            Trajet trajet = trajetRepository.findById(trajetId).orElse(null);

            if (trajet != null && trajet.getStatus() == TripStatus.Created) {

                trajet.setStatus(TripStatus.Canceled);
                trajetRepository.save(trajet);

                // 🔥 notifier client
                messagingTemplate.convertAndSend(
                        "/topic/client/" + trajet.getClient().getId(),
                        "Aucun chauffeur disponible"
                );
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}