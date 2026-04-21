package com.example.taximotoapp_backend.chat.service;

import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.chat.dto.request.ChatMessage;
import com.example.taximotoapp_backend.chat.dto.response.ChatResponse;
import com.example.taximotoapp_backend.chat.dto.response.MessageResponse;
import com.example.taximotoapp_backend.chat.mapper.ChatMapper;
import com.example.taximotoapp_backend.chat.model.Chat;
import com.example.taximotoapp_backend.chat.model.Message;
import com.example.taximotoapp_backend.chat.repository.ChatRepository;
import com.example.taximotoapp_backend.chat.repository.MessageRepository;
import com.example.taximotoapp_backend.model.enumClass.Role;
import com.example.taximotoapp_backend.model.enumClass.SenderType;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final TrajetRepository trajetRepository;
    private final ChatMapper chatMapper;

    // 🔹 créer ou récupérer chat
    public ChatResponse getOrCreateChat(Long trajetId) {
        Chat chat = chatRepository.findByTrajetId(trajetId)
                .orElseGet(() -> {
                    Trajet trajet = trajetRepository.findById(trajetId)
                            .orElseThrow(() -> new RuntimeException("Trajet not found"));
                    Chat newChat = new Chat();
                    newChat.setTrajet(trajet);
                    newChat.setCreatedAt(LocalDateTime.now());
                    return chatRepository.save(newChat);
                });
        return chatMapper.toChatResponse(chat);
    }

    // récupérer messages
    public List<MessageResponse> getMessages(Long chatId, String email) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        checkAccess(chat, email); // 🔐 sécurité

        List<Message> messages = messageRepository.findByChatIdOrderBySentAtAsc(chatId);
        return chatMapper.toMessageResponseList(messages);
    }

    // 🔹 envoyer message (logique métier)
    public MessageResponse sendMessage(ChatMessage dto, String email) {
        Chat chat = chatRepository.findById(dto.getChatId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        checkAccess(chat, email); // 🔐 sécurité
        // 🔐 restriction: chat only allowed if trajet is active
        Trajet trajet = chat.getTrajet();
        if (trajet.getStatus() == com.example.taximotoapp_backend.model.enumClass.TripStatus.Completed ||
                trajet.getStatus() == com.example.taximotoapp_backend.model.enumClass.TripStatus.Canceled) {
            throw new RuntimeException("Le trajet est terminé ou annulé. Le chat est désactivé.");
        }

        Message message = new Message();
        message.setChat(chat);
        message.setContenu(dto.getContenu());
        message.setSentAt(LocalDateTime.now());

        // 🔐 utilisateur récupéré depuis le paramètre (injecté depuis le contrôleur)
        User user=userRepository.findByEmail(email).orElseThrow();
        if (user.getRole()== Role.ROLE_CHAUFFEUR){
            message.setSenderType(SenderType.chauffeur);
        }else {
            message.setSenderType(SenderType.client);
        }
        Message saved = messageRepository.save(message);
        return chatMapper.toMessageResponse(saved);
    }

    private void checkAccess(Chat chat, String email) {
        User client = chat.getTrajet().getClient();
        User chauffeur = chat.getTrajet().getChauffeur();

        // Autorisé si c'est le client
        if (client.getEmail().equals(email)) return;

        // Autorisé si c'est le chauffeur (et qu'un chauffeur est assigné)
        if (chauffeur != null && chauffeur.getEmail().equals(email)) return;

        throw new RuntimeException("Access denied to this chat");
    }
}
