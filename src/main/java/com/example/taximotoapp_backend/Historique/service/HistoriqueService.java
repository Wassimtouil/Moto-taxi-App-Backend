package com.example.taximotoapp_backend.Historique.service;

import com.example.taximotoapp_backend.User.model.Client;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoriqueService {
    private final TrajetRepository trajetRepository;
    private final UserRepository userRepository;


}
