package com.example.taximotoapp_backend.nlpVocale.dto;

import com.example.taximotoapp_backend.model.enumClass.PaiementType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class NlpRequestDTO {

    private MultipartFile file;

    private String preferredDriverGender;
    private Long preferredDriverId;
    private PaiementType paymentMethod;
}