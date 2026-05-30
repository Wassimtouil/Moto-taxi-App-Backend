package com.example.taximotoapp_backend.geocode.service;

import com.example.taximotoapp_backend.geocode.dto.GeocodingSuggestionDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String SERP_API_KEY = "7c9bd56735d0fb2dd5522766b28db65c1c8183a2461a0f2b150b08103618d201";
    private static final String SERP_URL = "https://serpapi.com/search.json";

    public GeocodingService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<GeocodingSuggestionDTO> searchPlaces(String query, Double lat, Double lng) {
        try {
            String url = SERP_URL + "?engine=google_maps"
                    + "&q=" + java.net.URLEncoder.encode(query, "UTF-8")
                    + "&type=search"
                    + "&hl=fr"
                    + "&gl=tn"
                    + "&api_key=" + SERP_API_KEY;

            if (lat != null && lng != null) {
                url += "&ll=@" + lat + "," + lng + ",14z";
            }

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            List<GeocodingSuggestionDTO> results = new ArrayList<>();

            if (root.has("place_results")) {
                JsonNode p = root.get("place_results");
                GeocodingSuggestionDTO dto = extractSuggestion(p);
                if (dto != null) results.add(dto);
            }

            if (root.has("local_results")) {
                for (JsonNode r : root.get("local_results")) {
                    GeocodingSuggestionDTO dto = extractSuggestion(r);
                    if (dto != null) results.add(dto);
                }
            }

            // Sort by distance from origin if we have proximity
            if (lat != null && lng != null && results.size() > 1) {
                results.sort((a, b) -> {
                    double distA = haversine(lat, lng, a.getLat(), a.getLng());
                    double distB = haversine(lat, lng, b.getLat(), b.getLng());
                    return Double.compare(distA, distB);
                });
            }

            return results.size() > 10 ? results.subList(0, 10) : results;

        } catch (Exception e) {
            throw new RuntimeException("Erreur recherche SerpAPI", e);
        }
    }

    private GeocodingSuggestionDTO extractSuggestion(JsonNode node) {
        JsonNode coords = node.get("gps_coordinates");
        if (coords == null || !coords.has("latitude")) return null;

        GeocodingSuggestionDTO dto = new GeocodingSuggestionDTO();
        dto.setName(node.has("title") ? node.get("title").asText("") : "");
        dto.setAddress(node.has("address") ? node.get("address").asText("") : "");
        dto.setLat(coords.get("latitude").asDouble());
        dto.setLng(coords.get("longitude").asDouble());
        return dto;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
