package com.example.taximotoapp_backend.chat.controller;

import com.example.taximotoapp_backend.chat.dto.request.ChatMessage;
import com.example.taximotoapp_backend.chat.dto.response.ChatResponse;
import com.example.taximotoapp_backend.chat.dto.response.MessageResponse;
import com.example.taximotoapp_backend.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/chat")
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // Créer ou récupérer chat
    @PreAuthorize("hasAnyRole('CLIENT','CHAUFFEUR')")
    @GetMapping("/getChat/{trajetId}")
    public ResponseEntity<ChatResponse> getChatByTrajet(@PathVariable Long trajetId) {
        return ResponseEntity.ok(chatService.getOrCreateChat(trajetId));
    }

    //  Récupérer messages
    @PreAuthorize("hasAnyRole('CLIENT','CHAUFFEUR')")
    @GetMapping("/getMessages/{chatId}")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long chatId, Authentication authentication) {
        return ResponseEntity.ok(chatService.getMessages(chatId, authentication.getName()));
    }

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage dto, Principal principal) {
        MessageResponse response = chatService.sendMessage(dto, principal.getName());
        messagingTemplate.convertAndSend(
                "/topic/chat/" + dto.getChatId(),
                response
        );
    }
}
