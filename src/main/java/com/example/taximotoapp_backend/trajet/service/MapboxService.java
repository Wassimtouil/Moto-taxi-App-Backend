package com.example.taximotoapp_backend.trajet.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class MapboxService {

    @Value("${mapbox.access-token}")
    private String mapboxToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public String reverseGeocode(Double lat, Double lon) {
        String url = String.format(Locale.US,
                "https://api.mapbox.com/geocoding/v5/mapbox.places/%f,%f.json?access_token=%s&limit=1&language=ar&types=address,poi",
                lon, lat, mapboxToken
        );
        try {
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            List<?> features = (List<?>) response.get("features");
            if (features != null && !features.isEmpty()) {
                return ((Map<?, ?>) features.get(0)).get("place_name").toString();
            }
        } catch (Exception e) {
            System.err.println("Geocoding error: " + e.getMessage());
        }
        return String.format(Locale.US, "%.4f, %.4f", lat, lon);
    }

    public RouteDetails getRouteDetails(Double lat1, Double lon1, Double lat2, Double lon2) {
        String url = String.format(Locale.US,
                "https://api.mapbox.com/directions/v5/mapbox/driving/%f,%f;%f,%f?access_token=%s&geometries=polyline&overview=full",
                lon1, lat1, lon2, lat2, mapboxToken
        );
        try {
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            List<?> routes = (List<?>) response.get("routes");
            if (routes != null && !routes.isEmpty()) {
                Map<?, ?> route = (Map<?, ?>) routes.get(0);
                Double distance = ((Number) route.get("distance")).doubleValue() / 1000.0; // km
                Double duration = ((Number) route.get("duration")).doubleValue() / 60.0; // minutes
                String geometry = (String) route.get("geometry");

                return new RouteDetails(distance, duration.intValue(), geometry);
            }
        } catch (Exception e) {
            System.err.println("Directions error: " + e.getMessage());
        }

        // Fallback to Haversine-like distance estimate if API fails
        double dist = calculateHaversine(lat1, lon1, lat2, lon2);
        return new RouteDetails(dist, (int)(dist * 2), ""); // 2 min per km fallback
    }

    private double calculateHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RouteDetails {
        private Double distanceKm;
        private Integer durationMinutes;
        private String encodedPolyline;
    }
}
