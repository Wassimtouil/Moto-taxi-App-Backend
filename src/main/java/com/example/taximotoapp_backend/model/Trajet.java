package com.example.taximotoapp_backend.model;

import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import com.example.taximotoapp_backend.model.enumClass.TripType;
import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_type", nullable = false)
    private TripType tripType;

    @Column(name = "pickup_latitude", nullable = false)
    private Double pickupLatitude;

    @Column(name = "pickup_longitude", nullable = false)
    private Double pickupLongitude;

    @Column(name = "destination_latitude", nullable = false)
    private Double destinationLatitude;

    @Column(name = "destination_longitude", nullable = false)
    private Double destinationLongitude;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "price")
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;
    @PrePersist
    void onCreate(){
        requestedAt = LocalDateTime.now();
        status=TripStatus.Created;
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

    public TripType getTripType() {
        return tripType;
    }

    public Double getPickupLatitude() {
        return pickupLatitude;
    }

    public Double getPickupLongitude() {
        return pickupLongitude;
    }

    public Double getDestinationLatitude() {
        return destinationLatitude;
    }

    public Double getDestinationLongitude() {
        return destinationLongitude;
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

    public void setTripType(TripType tripType) {
        this.tripType = tripType;
    }

    public void setPickupLatitude(Double pickupLatitude) {
        this.pickupLatitude = pickupLatitude;
    }

    public void setPickupLongitude(Double pickupLongitude) {
        this.pickupLongitude = pickupLongitude;
    }

    public void setDestinationLatitude(Double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public void setDestinationLongitude(Double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
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
}
