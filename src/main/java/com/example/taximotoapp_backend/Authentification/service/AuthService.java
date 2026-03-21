package com.example.taximotoapp_backend.Authentification.service;

import com.example.taximotoapp_backend.Authentification.dto.LoginRequest;
import com.example.taximotoapp_backend.Authentification.dto.RegisterRequest;
import com.example.taximotoapp_backend.Authentification.response.AuthResponse;
import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.model.Client;
import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.model.enumClass.Gender;
import com.example.taximotoapp_backend.model.enumClass.Role;
import com.example.taximotoapp_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    //login service
    public AuthResponse login(LoginRequest request) {
        User user;
        if (request.getFirebaseUid() != null) {
            // Login via OAuth
            user = userRepository.findByFirebaseUid(request.getFirebaseUid())
                    .orElseThrow(() -> new RuntimeException("OAuth user not found"));
        } else {
            // Login classique
            Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
            if (optionalUser.isEmpty()) {
                throw new RuntimeException("User not found");
            }
            user = optionalUser.get();
            // Vérification du mot de passe
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        }
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getFullName(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse register(RegisterRequest request){
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        }catch (IllegalArgumentException e){
            throw new RuntimeException("Invalid role. Must be CLIENT or CHAUFFEUR");
        }
        Gender gender;
        try{
            gender=Gender.valueOf(request.getGender().toUpperCase());
        }catch (IllegalArgumentException e){
            throw new RuntimeException("Invalid gender. Must be female or male");
        }
        User user;
        if (role == Role.ROLE_CLIENT){
            user = new Client();
        }else {
            user = new Chauffeur();
        }
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setGender(gender);
        user.setIsVerified(true);
        user.setFirebaseUid(request.getFirebaseUid());
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getFullName(), user.getEmail(), user.getRole().name());
    }
}