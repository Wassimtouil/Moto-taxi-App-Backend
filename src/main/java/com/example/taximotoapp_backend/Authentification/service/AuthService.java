package com.example.taximotoapp_backend.Authentification.service;

import com.example.taximotoapp_backend.Admin.model.Admin;
import com.example.taximotoapp_backend.Authentification.dto.LoginRequest;
import com.example.taximotoapp_backend.Authentification.dto.RegisterRequest;
import com.example.taximotoapp_backend.Authentification.response.AuthResponse;
import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.model.Client;
import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.Admin.repository.AdminRepository;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.User.service.UserService;
import com.example.taximotoapp_backend.model.enumClass.ActivityStatus;
import com.example.taximotoapp_backend.model.enumClass.Availability;
import com.example.taximotoapp_backend.model.enumClass.Gender;
import com.example.taximotoapp_backend.model.enumClass.Role;
import com.example.taximotoapp_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    //login service
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getEmail() != null ? request.getEmail() : request.getFirebaseUid();

        // 1. Authentifier
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, request.getPassword())
        );

        // 2. Charger les détails
        org.springframework.security.core.userdetails.UserDetails userDetails = userService.loadUserByUsername(identifier);
        String token = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // 3. Réponse
        var userOpt = userRepository.findByEmail(identifier);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActivityStatus(ActivityStatus.ONLINE);
            if (user instanceof Chauffeur) ((Chauffeur) user).setAvailability(Availability.TRUE);
            userRepository.save(user);

            return new AuthResponse(token, refreshToken, user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(),
                    user.getGender() != null ? user.getGender().name() : null);
        }

        var adminOpt = adminRepository.findByUsername(identifier);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            return new AuthResponse(token, refreshToken, admin.getId(), admin.getUsername(), admin.getUsername(), "ROLE_ADMIN", null);
        }

        throw new RuntimeException("Erreur post-authentification");
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
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(
                token,
                refreshToken,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getGender() != null ? user.getGender().name() : null
        );
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String email = jwtService.extractEmail(refreshToken);
        org.springframework.security.core.userdetails.UserDetails userDetails = userService.loadUserByUsername(email);

        String newToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return new AuthResponse(newToken, newRefreshToken, user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(),
                    user.getGender() != null ? user.getGender().name() : null);
        }

        var adminOpt = adminRepository.findByUsername(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            return new AuthResponse(newToken, newRefreshToken, admin.getId(), admin.getUsername(), admin.getUsername(), "ROLE_ADMIN", null);
        }

        throw new RuntimeException("User not found for refresh token");
    }
}