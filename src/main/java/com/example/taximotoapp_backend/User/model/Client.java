package com.example.taximotoapp_backend.User.model;

import com.example.taximotoapp_backend.trajet.model.Trajet;
import jakarta.persistence.*;

import java.util.List;

@Entity
@PrimaryKeyJoinColumn(name = "user_id")
public class Client extends User{
    @OneToMany(mappedBy = "client",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Trajet> trajets;

    public List<Trajet> getTrajets() {
        return trajets;
    }
}
