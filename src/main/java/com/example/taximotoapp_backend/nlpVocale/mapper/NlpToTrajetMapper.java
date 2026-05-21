package com.example.taximotoapp_backend.nlpVocale.mapper;

import com.example.taximotoapp_backend.nlpVocale.dto.NlpRequestDTO;
import com.example.taximotoapp_backend.nlpVocale.dto.NlpResponseDTO;
import com.example.taximotoapp_backend.trajet.dto.request.TrajetRequest;
import org.springframework.stereotype.Component;

@Component
public class NlpToTrajetMapper {

    public TrajetRequest map(NlpRequestDTO request, NlpResponseDTO nlp) {

        TrajetRequest trajet = new TrajetRequest();

        // destination NLP
        trajet.setDestinationAddress(nlp.getPlace());

        if (nlp.getGps() != null) {
            trajet.setDestinationLatitude(nlp.getGps().getLat());
            trajet.setDestinationLongitude(nlp.getGps().getLng());
        }

        // user context
        trajet.setPreferredDriverGender(request.getPreferredDriverGender());
        trajet.setPreferredDriverId(request.getPreferredDriverId());
        trajet.setPaymentMethod(request.getPaymentMethod());

        return trajet;
    }
}