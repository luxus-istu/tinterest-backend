package com.luxus.tinterest.controller;

import com.luxus.tinterest.dto.chat.MessageSendRequestDto;
import com.luxus.tinterest.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    @MessageMapping("/chats/{chatId}/messages")
    public void sendMessage(@DestinationVariable Long chatId,
                            @Valid @Payload MessageSendRequestDto request,
                            Principal principal) {
        chatService.sendMessage(requireUserId(principal), chatId, request);
    }

    private Long requireUserId(Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authenticated user is missing");
        }
        return Long.parseLong(principal.getName());
    }
}
