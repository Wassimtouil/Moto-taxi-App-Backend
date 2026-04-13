package com.example.taximotoapp_backend.trajet.mapper;

import com.example.taximotoapp_backend.trajet.dto.request.TrajetRequest;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.dto.response.TrajetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrajetMapper {

    // Entity -> response (les coordonnées viennent maintenant de TrajetLocation)
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "chauffeur.id", target = "chauffeurId")
    @Mapping(source = "trajetLocation.pickupLatitude", target = "pickupLatitude")
    @Mapping(source = "trajetLocation.pickupLongitude", target = "pickupLongitude")
    @Mapping(source = "trajetLocation.destinationLatitude", target = "destinationLatitude")
    @Mapping(source = "trajetLocation.destinationLongitude", target = "destinationLongitude")
    @Mapping(source = "client.fullName", target = "clientName")
    @Mapping(source = "client.location.latitude", target = "clientLatitude")
    @Mapping(source = "client.location.longitude", target = "clientLongitude")
    TrajetResponse toDTO(Trajet trajet);

    // request -> Entity (ignorer les champs gérés manuellement dans le service)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "chauffeur", ignore = true)
    @Mapping(target = "trajetLocation", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "distanceKm", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "requestedAt", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "chat", ignore = true)
    Trajet toEntity(TrajetRequest request);
}
