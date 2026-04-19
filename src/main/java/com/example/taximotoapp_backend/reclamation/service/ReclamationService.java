package com.example.taximotoapp_backend.reclamation.service;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.reclamation.dto.request.ReclamationRequest;
import com.example.taximotoapp_backend.reclamation.dto.response.ReclamationResponse;
import com.example.taximotoapp_backend.reclamation.mapper.ReclamationMapper;
import com.example.taximotoapp_backend.reclamation.model.Reclamation;
import com.example.taximotoapp_backend.reclamation.repository.ReclamationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReclamationService {
    private final ReclamationRepository repository;
    private final UserRepository userRepository;
    private final ReclamationMapper mapper;
    public ReclamationResponse create(ReclamationRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Reclamation r = new Reclamation();
        r.setObjet(request.getObjet());
        r.setMessage(request.getContenu());
        r.setUser(user);
        Reclamation Rsaved=repository.save(r);
        return mapper.toResponse(Rsaved);
    }
}
