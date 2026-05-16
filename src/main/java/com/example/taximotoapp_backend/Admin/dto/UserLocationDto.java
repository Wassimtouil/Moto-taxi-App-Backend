package com.example.taximotoapp_backend.Admin.dto;


import com.example.taximotoapp_backend.model.enumClass.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLocationDto {
    private Long id;
    private String fullName;
    private Role role;
    private Double latitude;
    private Double longitude;
}