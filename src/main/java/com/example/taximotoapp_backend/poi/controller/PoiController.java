package com.example.taximotoapp_backend.poi.controller;

import com.example.taximotoapp_backend.poi.model.Poi;
import com.example.taximotoapp_backend.poi.service.PoiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/poi")
@RequiredArgsConstructor
public class PoiController {

    private final PoiService poiService;

    @GetMapping("/search")
    public Poi searchPoi(@RequestParam String text) {
        return poiService.findPoi(text);
    }

    @PostMapping("/add")
    public Poi addPoi(@RequestBody Poi poi) {
        return poiService.savePoi(poi);
    }

    @GetMapping("/all")
    public List<Poi> getAll() {
        return poiService.getAllPois();
    }

}