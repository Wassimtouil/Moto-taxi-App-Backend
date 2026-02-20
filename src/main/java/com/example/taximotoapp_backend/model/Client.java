package com.example.taximotoapp_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;

import java.util.List;

@Entity
@PrimaryKeyJoinColumn(name = "user_id")
public class Client extends User{
    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private List<Trajet> trajets;
}
