package com.example.taximotoapp_backend.reclamation.mapper;

import com.example.taximotoapp_backend.reclamation.dto.response.ReclamationResponse;
import com.example.taximotoapp_backend.reclamation.model.Reclamation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReclamationMapper {
    ReclamationResponse toResponse(Reclamation reclamation);

}
