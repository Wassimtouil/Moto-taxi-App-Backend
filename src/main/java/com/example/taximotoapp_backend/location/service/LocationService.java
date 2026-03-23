package com.example.taximotoapp_backend.location.service;

import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.repository.ChauffeurRepository;
import com.example.taximotoapp_backend.location.dto.LocationRequest;
import com.example.taximotoapp_backend.location.response.LocationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final ChauffeurRepository chauffeurRepository;

    public LocationResponse updateLocation(LocationRequest locationRequest){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Chauffeur chauffeur=chauffeurRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("Chauffeur not found"));
        chauffeur.setCurrent_latitude(locationRequest.getLatitude());
        chauffeur.setCurrent_longitude(locationRequest.getLongitude());
        chauffeurRepository.save(chauffeur);
        return new LocationResponse(locationRequest.getLatitude(),locationRequest.getLongitude(),"Location updated successfully");
    }
}
