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

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

}
