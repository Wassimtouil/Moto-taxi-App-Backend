package com.example.taximotoapp_backend.User.service;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.model.enumClass.ActivityStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserStatusScheduler {
    private final UserRepository userRepository;

    /**
     * Run every 20 seconds to clean up inactive users.
     */
    @Scheduled(fixedDelay = 20000)
    @Transactional
    public void checkUserHeartbeats() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(75);

        List<User> activeUsers = userRepository.findAll().stream()
                .filter(u -> u.getActivityStatus() == ActivityStatus.ONLINE)
                .toList();

        for (User user : activeUsers) {
            if (user.getLastSeenAt() == null || user.getLastSeenAt().isBefore(threshold)) {
                System.out.println("⏰ [SCHEDULER] User " + user.getEmail() + " has not updated in 75 seconds. Setting to OFFLINE.");
                user.setActivityStatus(ActivityStatus.OFFLINE);
                userRepository.save(user);
            }
        }
    }
}
