package com.example.taximotoapp_backend.poi.mapper;

import com.example.taximotoapp_backend.poi.dto.request.PoiRequest;
import com.example.taximotoapp_backend.poi.dto.response.PoiResponse;
import com.example.taximotoapp_backend.poi.model.Poi;
import org.springframework.stereotype.Component;

@Component
public class PoiMapper {
    public Poi toEntity(PoiRequest request) {
        if (request == null) return null;
        Poi poi = new Poi();
        poi.setName(request.getName());
        poi.setCategory(request.getCategory());
        poi.setCity(request.getCity());
        poi.setLatitude(request.getLatitude());
        poi.setLongitude(request.getLongitude());
        poi.setAddress(request.getAddress());
        return poi;
    }

    public PoiResponse toResponse(Poi poi) {
        if (poi == null) return null;
        PoiResponse response = new PoiResponse();
        response.setId(poi.getId());
        response.setName(poi.getName());
        response.setCategory(poi.getCategory());
        response.setCity(poi.getCity());
        response.setLatitude(poi.getLatitude());
        response.setLongitude(poi.getLongitude());
        response.setAddress(poi.getAddress());
        return response;
    }
}
