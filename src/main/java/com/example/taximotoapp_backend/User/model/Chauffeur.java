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

    private Double noteMoyenne;

    public Double getNoteMoyenne() {
        return noteMoyenne;
    }

    public void setNoteMoyenne(Double noteMoyenne) {
        this.noteMoyenne = noteMoyenne;
    }

    public List<Trajet> getTrajets() {
        return trajets;
    }

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



}
