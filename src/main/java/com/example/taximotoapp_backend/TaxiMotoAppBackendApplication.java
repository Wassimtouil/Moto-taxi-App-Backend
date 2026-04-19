package com.example.taximotoapp_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@SpringBootApplication
public class   TaxiMotoAppBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaxiMotoAppBackendApplication.class, args);
    }

}
