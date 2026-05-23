package com.example.taximotoapp_backend.ai.service;

import com.example.taximotoapp_backend.ai.dto.GrokResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GrokSQLService {

    private final WebClient webClient;

    @Value("${grok.api.key}")
    private String apiKey;

    public String generateSQL(String question) {

        String schema = """
    DATABASE STRUCTURE:

                TABLE `chauffeur` (
                  `note_moyenne` double DEFAULT NULL,
                  `user_id` bigint(20) NOT NULL,
                  `photo_url` varchar(255) DEFAULT NULL,
                  `vehicle_model` varchar(255) DEFAULT NULL,
                  `vehicle_plate` varchar(255) DEFAULT NULL,
                  `availability` enum('TRUE','FALSE') DEFAULT NULL,
                  `carte_grise_base64` longtext DEFAULT NULL,
                  `driving_licence_base64` longtext DEFAULT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                CREATE TABLE `admin` (
                  `user_id` bigint(20) NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                TABLE `chat` (
                  `created_at` datetime(6) NOT NULL,
                  `id` bigint(20) NOT NULL,
                  `trajet_id` bigint(20) NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                TABLE `client` (
                  `user_id` bigint(20) NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                TABLE `evaluation` (
                  `note` double NOT NULL,
                  `note_comportement` int(11) DEFAULT NULL,
                  `note_conduite` int(11) DEFAULT NULL,
                  `note_experience` int(11) DEFAULT NULL,
                  `note_ponctualite` int(11) DEFAULT NULL,
                  `note_service` int(11) DEFAULT NULL,
                  `note_vehicule` int(11) DEFAULT NULL,
                  `chauffeur_id` bigint(20) DEFAULT NULL,
                  `client_id` bigint(20) DEFAULT NULL,
                  `date_evaluation` datetime(6) DEFAULT NULL,
                  `id` bigint(20) NOT NULL,
                  `trajet_id` bigint(20) DEFAULT NULL,
                  `commentaire` varchar(255) DEFAULT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                TABLE `location` (
                  `latitude` double DEFAULT NULL,
                  `longitude` double DEFAULT NULL,
                  `id` bigint(20) NOT NULL,
                  `user_id` bigint(20) NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                TABLE `message` (
                  `chat_id` bigint(20) NOT NULL,
                  `id` bigint(20) NOT NULL,
                  `sent_at` datetime(6) NOT NULL,
                  `contenu` text NOT NULL,
                  `sender_type` enum('chauffeur','client') NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                TABLE `notifications` (
                  `id` bigint(20) NOT NULL,
                  `created_at` datetime(6) NOT NULL,
                  `driver_id` bigint(20) DEFAULT NULL,
                  `is_read` bit(1) NOT NULL,
                  `message` varchar(255) NOT NULL,
                  `title` varchar(255) NOT NULL,
                  `type` varchar(255) NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                
                TABLE `paiement` (
                  `montant` double DEFAULT NULL,
                  `date_paiement` datetime(6) DEFAULT NULL,
                  `id` bigint(20) NOT NULL,
                  `trajet_id` bigint(20) NOT NULL,
                  `status` enum('EN_ATTENTE','PAYE') DEFAULT NULL,
                  `type` enum('ONLINE','CASH') DEFAULT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                TABLE `payment_card` (
                  `is_default` bit(1) DEFAULT NULL,
                  `created_at` datetime(6) NOT NULL,
                  `id` bigint(20) NOT NULL,
                  `user_id` bigint(20) NOT NULL,
                  `brand` varchar(255) DEFAULT NULL,
                  `card_holder_name` varchar(255) DEFAULT NULL,
                  `expiry_month` varchar(255) DEFAULT NULL,
                  `expiry_year` varchar(255) DEFAULT NULL,
                  `last4digits` varchar(255) DEFAULT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                TABLE `reclamation` (
                  `date_reclamation` date DEFAULT NULL,
                  `id` bigint(20) NOT NULL,
                  `user_id` bigint(20) NOT NULL,
                  `admin_response` varchar(255) DEFAULT NULL,
                  `message` varchar(255) DEFAULT NULL,
                  `objet` enum('COURSE_ISSUE','PAYMENT_ISSUE','USER_BEHAVIOR','VEHICLE_ISSUE','APP_TECHNICAL_ISSUE','DELAY_OR_WAITING_TIME','SAFETY_PROBLEM','SERVICE_QUALITY','OTHER') DEFAULT NULL,
                  `reclamation_status` enum('EN_ATTENTE','EN_COURS','RESOLU') DEFAULT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                TABLE `trajet` (
                  `distance_km` double DEFAULT NULL,
                  `duration_minutes` int(11) DEFAULT NULL,
                  `price` double DEFAULT NULL,
                  `chauffeur_id` bigint(20) DEFAULT NULL,
                  `client_id` bigint(20) NOT NULL,
                  `completed_at` datetime(6) DEFAULT NULL,
                  `id` bigint(20) NOT NULL,
                  `preferred_driver_id` bigint(20) DEFAULT NULL,
                  `requested_at` datetime(6) DEFAULT NULL,
                  `scheduled_at` datetime(6) DEFAULT NULL,
                  `started_at` datetime(6) DEFAULT NULL,
                  `preferred_driver_gender` varchar(10) DEFAULT NULL,
                  `cancelled_by` varchar(20) DEFAULT NULL,
                  `payment_method` enum('ONLINE','CASH') DEFAULT NULL,
                  `status` enum('Created','Scheduled','Accepted','Arrived','Started','Completed','Canceled') NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                TABLE `trajet_location` (
                  `destination_latitude` double DEFAULT NULL,
                  `destination_longitude` double DEFAULT NULL,
                  `pickup_latitude` double DEFAULT NULL,
                  `pickup_longitude` double DEFAULT NULL,
                  `id` bigint(20) NOT NULL,
                  `trajet_id` bigint(20) NOT NULL,
                  `destination_address` varchar(255) DEFAULT NULL,
                  `encoded_polyline` text DEFAULT NULL,
                  `pickup_address` varchar(255) DEFAULT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                TABLE `transaction` (
                  `amount` double DEFAULT NULL,
                  `created_at` datetime(6) NOT NULL,
                  `id` bigint(20) NOT NULL,
                  `wallet_id` bigint(20) NOT NULL,
                  `description` varchar(255) DEFAULT NULL,
                  `status` enum('PENDING','COMPLETED','FAILED') DEFAULT NULL,
                  `type` enum('DEPOSIT','WITHDRAWAL','PAYMENT') DEFAULT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                TABLE `user` (
                  `age` int(11) NOT NULL,
                  `is_verified` bit(1) NOT NULL,
                  `created_at` datetime(6) NOT NULL,
                  `id` bigint(20) NOT NULL,
                  `email` varchar(255) DEFAULT NULL,
                  `firebase_uid` varchar(255) DEFAULT NULL,
                  `full_name` varchar(255) DEFAULT NULL,
                  `password` varchar(255) DEFAULT NULL,
                  `activity_status` enum('ONLINE','OFFLINE') DEFAULT NULL,
                  `gender` enum('MALE','FEMALE') DEFAULT NULL,
                  `photo_base64` longtext DEFAULT NULL,
                  `role` enum('ROLE_CLIENT','ROLE_CHAUFFEUR','ROLE_ADMIN') DEFAULT NULL,
                  `last_seen_at` datetime(6) DEFAULT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                TABLE `wallet` (
                  `balance` double DEFAULT NULL,
                  `cash_balance` double DEFAULT NULL,
                  `created_at` datetime(6) NOT NULL,
                  `id` bigint(20) NOT NULL,
                  `updated_at` datetime(6) DEFAULT NULL,
                  `user_id` bigint(20) DEFAULT NULL,
                  `currency` varchar(255) DEFAULT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                
    """;

        String prompt = """
    You are a senior SQL expert.

    %s

                CRITICAL RULES:
                - Output ONLY SQL
                - No text
                - No explanation
                - No markdown
                - No formatting
                - Start immediately with SELECT
                - Only SELECT queries allowed
                - Must use LIMIT 10
                - Use only provided tables/columns
                - If unsure, still generate best SELECT
OUTPUT FORMAT:
ONLY RAW SQL
    USER QUESTION:
    %s
    """.formatted(schema, question);

        return callGrok(prompt);
    }

    public String simpleText(String prompt) {
        return callGrok(prompt);
    }

    private String callGrok(String prompt) {

        Map<String, Object> body = Map.of(
                "model", "grok-3-mini",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0
        );

        GrokResponse response = webClient.post()
                .uri("/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GrokResponse.class)
                .block();

        if (response == null || response.getChoices().isEmpty()) {
            throw new RuntimeException("Empty Grok response");
        }

        return response.getChoices()
                .get(0)
                .getMessage()
                .getContent();
    }
}