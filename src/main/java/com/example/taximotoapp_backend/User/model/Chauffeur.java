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
    private double current_latitude;
    private double current_longitude;
    private Availability availability;

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public double getCurrent_latitude() {
        return current_latitude;
    }
    public double getCurrent_longitude() {
        return current_longitude;
    }
    public void setCurrent_latitude(double current_latitude) {
        this.current_latitude = current_latitude;
    }
    public void setCurrent_longitude(double current_longitude) {
        this.current_longitude = current_longitude;
    }
}
