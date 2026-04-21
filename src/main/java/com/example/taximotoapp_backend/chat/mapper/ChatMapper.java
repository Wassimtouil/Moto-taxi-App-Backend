package com.example.taximotoapp_backend.chat.mapper;

import com.example.taximotoapp_backend.chat.dto.response.ChatResponse;
import com.example.taximotoapp_backend.chat.dto.response.MessageResponse;
import com.example.taximotoapp_backend.chat.model.Chat;
import com.example.taximotoapp_backend.chat.model.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    @Mapping(source = "id", target = "chatId")
    @Mapping(source = "trajet.id", target = "trajetId")
    ChatResponse toChatResponse(Chat chat);

    @Mapping(source = "chat.id", target = "chatId")
    @Mapping(source = "senderType", target = "sender")
    MessageResponse toMessageResponse(Message message);

    List<MessageResponse> toMessageResponseList(List<Message> messages);
}
