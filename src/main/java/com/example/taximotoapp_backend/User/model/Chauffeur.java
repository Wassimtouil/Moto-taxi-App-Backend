package com.example.taximotoapp_backend.User.model;

import com.example.taximotoapp_backend.model.enumClass.Availability;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import jakarta.persistence.*;

import java.util.List;

@Entity
@PrimaryKeyJoinColumn(name = "user_id")
public class Chauffeur extends User {
    @OneToMany(mappedBy = "chauffeur", fetch = FetchType.LAZY)
    private List<Trajet> trajets;
    @Enumerated(EnumType.STRING)
    private Availability availability;
    private String vehicleModel;
    private String vehiclePlate;
    private String photoUrl;
    private Double rating;

    private Double noteMoyenne;

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

}
