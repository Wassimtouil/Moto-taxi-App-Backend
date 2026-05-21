package com.example.taximotoapp_backend.nlpVocale.service;


import com.example.taximotoapp_backend.nlpVocale.client.PythonNlpClient;
import com.example.taximotoapp_backend.nlpVocale.dto.NlpRequestDTO;
import com.example.taximotoapp_backend.nlpVocale.dto.NlpResponseDTO;
import com.example.taximotoapp_backend.nlpVocale.mapper.NlpToTrajetMapper;
import com.example.taximotoapp_backend.trajet.dto.request.TrajetRequest;
import com.example.taximotoapp_backend.trajet.dto.response.TrajetResponse;
import com.example.taximotoapp_backend.trajet.service.TrajetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class NlpService {

    private final PythonNlpClient pythonNlpClient;
    private final TrajetService trajetService;
    private final NlpToTrajetMapper mapper;

    public TrajetResponse processAndCreateTrajet(NlpRequestDTO request) {

        // 1. Python NLP
        NlpResponseDTO nlp =
                pythonNlpClient.callPython(request.getFile());

        // 2. Mapping
        TrajetRequest trajetRequest =
                mapper.map(request, nlp);

        // 3. Business logic
        return trajetService.createTrajet(trajetRequest);
    }
}