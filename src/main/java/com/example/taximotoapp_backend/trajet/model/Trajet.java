package com.example.taximotoapp_backend.trajet.model;

import com.example.taximotoapp_backend.Evaluation.model.Evaluation;
import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.model.Client;
import com.example.taximotoapp_backend.chat.model.Chat;
import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import com.example.taximotoapp_backend.paiement.model.Paiement;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Trajet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relation Client
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // Relation Chauffeur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chauffeur_id")
    private Chauffeur chauffeur;

    @OneToOne(mappedBy = "trajet", cascade = CascadeType.ALL)
    private TrajetLocation trajetLocation; // nouvelle classe pour positions

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "price")
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TripStatus status;

    @OneToOne(mappedBy = "trajet",cascade = CascadeType.ALL)
    private Chat chat;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "preferred_driver_gender", length = 10)
    private String preferredDriverGender;

    @OneToOne(mappedBy = "trajet")
    private Evaluation evaluation;

    @OneToOne(mappedBy = "trajet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Paiement paiement;


    @PrePersist
    protected void onCreate(){
        requestedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TripStatus.Created;
        }
    }
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public Long getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public Chauffeur getChauffeur() {
        return chauffeur;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public Double getPrice() {
        return price;
    }

    public TripStatus getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setChauffeur(Chauffeur chauffeur) {
        this.chauffeur = chauffeur;
    }


    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public TrajetLocation getTrajetLocation() {
        return trajetLocation;
    }

    public void setTrajetLocation(TrajetLocation trajetLocation) {
        this.trajetLocation = trajetLocation;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getPreferredDriverGender() {
        return preferredDriverGender;
    }

    public void setPreferredDriverGender(String preferredDriverGender) {
        this.preferredDriverGender = preferredDriverGender;
    }
}
