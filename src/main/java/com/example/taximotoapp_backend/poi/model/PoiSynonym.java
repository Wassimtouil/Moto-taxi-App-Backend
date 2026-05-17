package com.example.taximotoapp_backend.poi.model;

import jakarta.persistence.*;

@Entity
public class PoiSynonym {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String synonym;
    @ManyToOne
    @JoinColumn(name = "poi_id")
    private Poi poi;

    public Poi getPoi() {
        return poi;
    }

    public String getSynonym() {
        return synonym;
    }

    public Long getId() {
        return id;
    }

    public void setSynonym(String synonym) {
        this.synonym = synonym;
    }

    public void setPoi(Poi poi) {
        this.poi = poi;
    }
}