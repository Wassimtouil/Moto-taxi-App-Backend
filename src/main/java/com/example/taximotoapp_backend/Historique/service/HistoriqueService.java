package com.example.taximotoapp_backend.Historique.service;

import com.example.taximotoapp_backend.Historique.dto.response.HistoriqueClientResponse;
import com.example.taximotoapp_backend.Historique.mapper.HistoriqueMapper;
import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.model.Client;
import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.ChauffeurRepository;
import com.example.taximotoapp_backend.User.repository.ClientRepository;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoriqueService {
    private final TrajetRepository trajetRepository;
    private final ClientRepository clientRepository;
    private final ChauffeurRepository chauffeurRepository;
    private final UserRepository userRepository;
    private final HistoriqueMapper mapper;

    public List<HistoriqueClientResponse> getHistoriqueClient() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User introuvable"));
        if (!(user instanceof Client client)) {
            throw new RuntimeException("Accès refusé : utilisateur non client");
        }
        return client.getTrajets()
                .stream()
                .map(mapper::toHistoriqueClientResponse)
                .toList();
    }


}
