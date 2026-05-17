package com.example.taximotoapp_backend.poi.service;

import com.example.taximotoapp_backend.poi.model.Poi;
import com.example.taximotoapp_backend.poi.repository.PoiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoiService {
    private final PoiRepository poiRepository;

    // Recherche par nom ou synonyme
    public Poi findPoi(String text) {
        List<Poi> results = poiRepository.search(text);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    // Ajouter un POI
    public Poi savePoi(Poi poi) {
        return poiRepository.save(poi);
    }

    // Liste complète
    public List<Poi> getAllPois() {
        return poiRepository.findAll();
    }

}
