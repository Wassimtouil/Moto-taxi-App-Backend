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

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String drivingLicenceBase64;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String carteGriseBase64;


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





    public void setTrajets(List<Trajet> trajets) {
        this.trajets = trajets;
    }

    public String getDrivingLicenceBase64() {
        return drivingLicenceBase64;
    }

    public void setDrivingLicenceBase64(String drivingLicenceBase64) {
        this.drivingLicenceBase64 = drivingLicenceBase64;
    }

    public String getCarteGriseBase64() {
        return carteGriseBase64;
    }

    public void setCarteGriseBase64(String carteGriseBase64) {
        this.carteGriseBase64 = carteGriseBase64;
    }

    @PrePersist
    @PreUpdate
    protected void syncAvailabilityWithActivityStatus() {
        if (this.getActivityStatus() == com.example.taximotoapp_backend.model.enumClass.ActivityStatus.ONLINE) {
            this.availability = Availability.TRUE;
        } else if (this.getActivityStatus() == com.example.taximotoapp_backend.model.enumClass.ActivityStatus.OFFLINE) {
            this.availability = Availability.FALSE;
        }
    }
}
