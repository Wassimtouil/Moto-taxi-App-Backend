package com.example.taximotoapp_backend.poi.controller;

import com.example.taximotoapp_backend.poi.dto.request.PoiRequest;
import com.example.taximotoapp_backend.poi.dto.response.PoiResponse;
import com.example.taximotoapp_backend.poi.model.Poi;
import com.example.taximotoapp_backend.poi.service.PoiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/poi")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PoiController {

    private final PoiService poiService;

    @GetMapping("/search")
    public PoiResponse search(@RequestParam String text) {
        return poiService.searchPoi(text);
    }

    @PostMapping("/add")
    public PoiResponse add(@RequestBody PoiRequest request) {
        return poiService.addPoi(request);
    }

    @GetMapping("/all")
    public List<PoiResponse> getAll() {
        return poiService.getAll();
    }
}