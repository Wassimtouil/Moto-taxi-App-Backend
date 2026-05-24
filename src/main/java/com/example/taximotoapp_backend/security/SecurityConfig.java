package com.example.taximotoapp_backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers("/api/poi/**").permitAll()
                        .requestMatchers("/api/location/**").hasAnyRole("CHAUFFEUR", "CLIENT")

                        .requestMatchers("/api/historique/**").hasAnyRole("CLIENT","CHAUFFEUR")
                        .requestMatchers("/api/evaluation/**").hasAnyRole("CLIENT","CHAUFFEUR")
                        .requestMatchers("/api/trajets/**").hasAnyRole("CLIENT","CHAUFFEUR")
                        .requestMatchers("/api/chat/**").hasAnyRole("CLIENT","CHAUFFEUR")

                        .requestMatchers("/ws/**").permitAll() // 🔥 IMPORTANT

                        // Swagger endpoints
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()

                        // Role CHAUFFEUR
                        .requestMatchers("/api/chauffeur/**").hasRole("CHAUFFEUR")

                        // ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .requestMatchers("/api/ia/**").permitAll()

                        .requestMatchers("/api/report/**").permitAll()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(exc -> exc
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
                        })
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
