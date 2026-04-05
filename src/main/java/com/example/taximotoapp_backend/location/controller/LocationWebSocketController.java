package com.example.taximotoapp_backend.location.controller;

import com.example.taximotoapp_backend.location.dto.LocationRequest;
import com.example.taximotoapp_backend.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class LocationWebSocketController {
    private final LocationService locationService;

    @MessageMapping("/location.update")
    public void updateLocation(LocationRequest locationRequest, SimpMessageHeaderAccessor headerAccessor) {
        // Extract username from WebSocket session attributes (set during JWT handshake)
        String email = (String) headerAccessor.getSessionAttributes().get("username");
        
        if (email == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        locationService.updateLocation(locationRequest, email);
    }
}
