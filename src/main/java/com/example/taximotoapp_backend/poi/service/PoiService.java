package com.example.taximotoapp_backend.poi.service;

import com.example.taximotoapp_backend.poi.dto.request.PoiRequest;
import com.example.taximotoapp_backend.poi.dto.response.PoiResponse;
import com.example.taximotoapp_backend.poi.mapper.PoiMapper;
import com.example.taximotoapp_backend.poi.model.Poi;
import com.example.taximotoapp_backend.poi.repository.PoiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PoiService {

    private final PoiRepository poiRepository;
    private final PoiMapper poiMapper;

    public PoiResponse searchPoi(String text) {
        return poiRepository.search(text)
                .stream()
                .findFirst()
                .map(poiMapper::toResponse)
                .orElse(null);
    }

    public PoiResponse addPoi(PoiRequest request) {

        Poi poi = poiMapper.toEntity(request);
        Poi saved = poiRepository.save(poi);

        return poiMapper.toResponse(saved);
    }

    public List<PoiResponse> getAll() {

        return poiRepository.findAll()
                .stream()
                .map(poiMapper::toResponse)
                .collect(Collectors.toList());
    }
}
