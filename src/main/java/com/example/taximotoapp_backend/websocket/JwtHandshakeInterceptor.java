package com.example.taximotoapp_backend.websocket;

import com.example.taximotoapp_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            System.out.println("🔍 [JwtHandshakeInterceptor] Token from request: " + (token != null ? "PRESENT" : "NULL"));
            if (token != null) {
                boolean isValid = jwtService.isTokenValid(token);
                System.out.println("🔍 [JwtHandshakeInterceptor] isTokenValid: " + isValid);
                if (isValid) {
                    String username = jwtService.extractUsername(token);
                    attributes.put("username", username);
                    System.out.println("✅ [JwtHandshakeInterceptor] Handshake successful for user: " + username);
                    return true;
                }
            }
        }
        System.out.println("❌ [JwtHandshakeInterceptor] Handshake rejected. Token invalid or missing.");
        response.setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
        return false; // refuse connexion si JWT invalide
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {}
}
