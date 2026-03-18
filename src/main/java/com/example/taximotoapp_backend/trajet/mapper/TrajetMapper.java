package com.example.taximotoapp_backend.trajet.mapper;

import com.example.taximotoapp_backend.trajet.dto.TrajetRequest;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.response.TrajetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrajetMapper {

    // Entity -> response
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "chauffeur.id", target = "chauffeurId")
    TrajetResponse toDTO(Trajet trajet);

    // request -> Entity
    Trajet toEntity(TrajetRequest request);
}
