package com.example.taximotoapp_backend.User.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationStatDTO {
    private String date;
    private Long count;
}
