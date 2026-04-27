package com.example.taximotoapp_backend.reclamation.service;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.model.enumClass.ReclamationStatus;
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
        System.out.println(Rsaved.getMessage());
        return mapper.toResponse(Rsaved);
    }
    public List<ReclamationResponse> getMyReclamations() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        List<Reclamation> reclamations = repository.findByUser(user);

        return reclamations.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public ReclamationResponse update(Long id, ReclamationRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Reclamation reclamation = repository.findById(id).orElseThrow(() -> new RuntimeException("Reclamation not found"));
        if (!reclamation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        if (!ReclamationStatus.EN_ATTENTE.equals(reclamation.getReclamationStatus())){
            throw new RuntimeException("reclamation deja traitée ou résolue par l'admin");
        }
        reclamation.setObjet(request.getObjet());
        reclamation.setMessage(request.getContenu());
        Reclamation updated = repository.save(reclamation);
        return mapper.toResponse(updated);
    }
    public void delete(Long id){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Reclamation reclamation = repository.findById(id).orElseThrow(() -> new RuntimeException("Reclamation not found"));
        if (!reclamation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        if (!ReclamationStatus.EN_ATTENTE.equals(reclamation.getReclamationStatus())){
            throw new RuntimeException("reclamation deja traitée ou résolue par l'admin");
        }
        repository.delete(reclamation);
    }
}
