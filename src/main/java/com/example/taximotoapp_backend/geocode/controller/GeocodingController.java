package com.example.taximotoapp_backend.geocode.controller;

import com.example.taximotoapp_backend.geocode.dto.GeocodingSuggestionDTO;
import com.example.taximotoapp_backend.geocode.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/geocode")
@CrossOrigin("*")
@RequiredArgsConstructor
public class GeocodingController {

    private final GeocodingService geocodingService;

    @GetMapping("/search")
    public List<GeocodingSuggestionDTO> search(
            @RequestParam("q") String query,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lng", required = false) Double lng
    ) {
        return geocodingService.searchPlaces(query, lat, lng);
    }
}
