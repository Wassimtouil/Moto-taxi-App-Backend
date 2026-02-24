package com.example.taximotoapp_backend.Authentification.service;

import com.example.taximotoapp_backend.Authentification.dto.LoginRequest;
import com.example.taximotoapp_backend.Authentification.response.AuthResponse;
import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
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
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getFullName(), user.getEmail(), user.getRole().name());
    }
}
