package com.example.taximotoapp_backend;

import com.example.taximotoapp_backend.Admin.model.Admin;
import com.example.taximotoapp_backend.Admin.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableScheduling
@EnableAsync
@SpringBootApplication
public class TaxiMotoAppBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaxiMotoAppBackendApplication.class, args);
    }
}
