package com.example.taximotoapp_backend.trajet.mapper;

import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import com.example.taximotoapp_backend.trajet.dto.request.TrajetRequest;
import com.example.taximotoapp_backend.trajet.dto.response.ChauffeurStatResponse;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.dto.response.TrajetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TrajetMapper {

    default ChauffeurStatResponse toChauffeurStatResponse(Chauffeur chauffeur, List<Trajet> trajets) {
        if (chauffeur == null) return null;
        ChauffeurStatResponse response = new ChauffeurStatResponse();
        response.setId(chauffeur.getId().longValue());
        response.setFullName(chauffeur.getFullName());
        response.setEmail(chauffeur.getEmail());
        response.setVehicleModel(chauffeur.getVehicleModel());
        response.setVehiclePlate(chauffeur.getVehiclePlate());
        response.setPhotoUrl(chauffeur.getPhotoUrl());
        response.setRating(chauffeur.getNoteMoyenne());
        response.setAvailability(chauffeur.getAvailability());
        response.setVerified(chauffeur.getIsVerified());
        if (trajets != null) {
            response.setTotalTrips(trajets.size());
            response.setCompletedTrips(trajets.stream().filter(t -> t.getStatus() == TripStatus.Completed).count());
            response.setCanceledTrips(trajets.stream().filter(t -> t.getStatus() == TripStatus.Canceled).count());
            response.setTotalRevenue(trajets.stream()
                    .filter(t -> t.getStatus() == TripStatus.Completed && t.getPrice() != null)
                    .mapToDouble(Trajet::getPrice)
                    .sum());
            response.setTotalWorkTimeMinutes(trajets.stream()
                    .filter(t -> t.getStatus() == TripStatus.Completed && t.getDurationMinutes() != null)
                    .mapToLong(Trajet::getDurationMinutes)
                    .sum());
        }

        return response;
    }


    // Entity -> response (les coordonnées viennent maintenant de TrajetLocation)
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "chauffeur.id", target = "chauffeurId")
    @Mapping(source = "trajetLocation.pickupLatitude", target = "pickupLatitude")
    @Mapping(source = "trajetLocation.pickupLongitude", target = "pickupLongitude")
    @Mapping(source = "trajetLocation.destinationLatitude", target = "destinationLatitude")
    @Mapping(source = "trajetLocation.destinationLongitude", target = "destinationLongitude")
    @Mapping(source = "trajetLocation.pickupAddress", target = "pickupAddress")
    @Mapping(source = "trajetLocation.destinationAddress", target = "destinationAddress")
    @Mapping(source = "trajetLocation.encodedPolyline", target = "encodedPolyline")
    @Mapping(source = "client.fullName", target = "clientName")
    @Mapping(source = "client.location.latitude", target = "clientLatitude")
    @Mapping(source = "client.location.longitude", target = "clientLongitude")
    @Mapping(source = "paiement.type", target = "paymentMethod")
    @Mapping(source = "durationMinutes", target = "durationMinutes")
    TrajetResponse toDTO(Trajet trajet);

    // request -> Entity (ignorer les champs gérés manuellement dans le service)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "chauffeur", ignore = true)
    @Mapping(target = "trajetLocation", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "distanceKm", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "requestedAt", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "chat", ignore = true)
    Trajet toEntity(TrajetRequest request);
}
