package com.example.taximotoapp_backend.nlpVocale.controller;

import com.example.taximotoapp_backend.model.enumClass.PaiementType;
import com.example.taximotoapp_backend.nlpVocale.dto.NlpRequestDTO;
import com.example.taximotoapp_backend.nlpVocale.dto.NlpResponseDTO;
import com.example.taximotoapp_backend.nlpVocale.service.NlpService;
import com.example.taximotoapp_backend.trajet.dto.response.TrajetResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/nlp")
@CrossOrigin("*")
@RequiredArgsConstructor
public class NlpController {

    private final NlpService nlpService;

    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TrajetResponse processVoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam("preferredDriverGender") String preferredDriverGender,
            @RequestParam("preferredDriverId") Long preferredDriverId,
            @RequestParam("paymentMethod") PaiementType paymentMethod
    ) {

        NlpRequestDTO request = new NlpRequestDTO();
        request.setFile(file);
        request.setPreferredDriverGender(preferredDriverGender);
        request.setPreferredDriverId(preferredDriverId);
        request.setPaymentMethod(paymentMethod);

        return nlpService.processAndCreateTrajet(request);
    }

    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NlpResponseDTO transcribeVoice(
            @RequestParam("file") MultipartFile file
    ) {
        return nlpService.transcribe(file);
    }
}