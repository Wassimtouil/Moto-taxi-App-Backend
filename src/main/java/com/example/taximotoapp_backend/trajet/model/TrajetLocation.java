package com.example.taximotoapp_backend.trajet.model;

import jakarta.persistence.*;

@Entity
public class TrajetLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trajet_id", nullable = false)
    private Trajet trajet;

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

    public Trajet getTrajet() {
        return trajet;
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

    public void setTrajet(Trajet trajet) {
        this.trajet = trajet;
    }
}