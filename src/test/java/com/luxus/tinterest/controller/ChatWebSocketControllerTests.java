package com.luxus.tinterest.controller;

import com.luxus.tinterest.dto.chat.MessageSendRequestDto;
import com.luxus.tinterest.entity.MessageType;
import com.luxus.tinterest.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Chat WebSocket Controller Tests")
class ChatWebSocketControllerTests {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatWebSocketController chatWebSocketController;

    @Test
    @DisplayName("Should forward WebSocket chat message to service")
    void testSendMessage() {
        Principal principal = () -> "1";
        MessageSendRequestDto request = new MessageSendRequestDto(MessageType.TEXT, "Hello via websocket");

        chatWebSocketController.sendMessage(10L, request, principal);

        verify(chatService).sendMessage(1L, 10L, request);
    }

    @Test
    @DisplayName("Should reject WebSocket calls without authenticated principal")
    void testSendMessageWithoutPrincipal() {
        MessageSendRequestDto request = new MessageSendRequestDto(MessageType.TEXT, "Hello via websocket");

        assertThrows(AccessDeniedException.class,
                () -> chatWebSocketController.sendMessage(10L, request, null));
    }

    @Test
    @DisplayName("Should reject WebSocket calls when principal name is not numeric")
    void testSendMessageWithInvalidPrincipalName() {
        Principal principal = () -> "not-a-number";
        MessageSendRequestDto request = new MessageSendRequestDto(MessageType.TEXT, "Hello via websocket");

        assertThrows(NumberFormatException.class,
                () -> chatWebSocketController.sendMessage(10L, request, principal));
    }
}
