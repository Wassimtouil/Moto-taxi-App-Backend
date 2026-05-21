package com.example.taximotoapp_backend.nlpVocale.client;

import com.example.taximotoapp_backend.nlpVocale.MultipartInputStreamFileResource;
import com.example.taximotoapp_backend.nlpVocale.dto.NlpResponseDTO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class PythonNlpClient {

    private final RestTemplate restTemplate;

    private final String PYTHON_URL = "http://localhost:8000/nlp/process";

    public PythonNlpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public NlpResponseDTO callPython(MultipartFile file) {

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            return restTemplate.postForObject(
                    PYTHON_URL,
                    request,
                    NlpResponseDTO.class
            );

        } catch (Exception e) {
            throw new RuntimeException("Erreur upload audio Python", e);
        }
    }
}