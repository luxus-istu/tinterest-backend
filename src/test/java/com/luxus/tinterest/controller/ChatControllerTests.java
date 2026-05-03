package com.luxus.tinterest.controller;

import com.luxus.tinterest.dto.chat.ChatMessageResponseDto;
import com.luxus.tinterest.dto.chat.ChatMessagesPageResponseDto;
import com.luxus.tinterest.dto.chat.ChatSummaryResponseDto;
import com.luxus.tinterest.dto.chat.DirectChatRequestDto;
import com.luxus.tinterest.dto.chat.GroupChatCreateRequestDto;
import com.luxus.tinterest.dto.chat.MessageSendRequestDto;
import com.luxus.tinterest.entity.ChatType;
import com.luxus.tinterest.entity.MessageType;
import com.luxus.tinterest.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Chat Controller Tests")
class ChatControllerTests {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Should return current user chats")
    void testGetMyChats() {
        ChatSummaryResponseDto chatSummary = new ChatSummaryResponseDto(
                1L,
                ChatType.DIRECT,
                "Direct chat",
                1L,
                LocalDateTime.now(),
                List.of(),
                null,
                0L
        );

        when(chatService.getMyChats(1L)).thenReturn(List.of(chatSummary));

        ResponseEntity<List<ChatSummaryResponseDto>> result = chatController.getMyChats(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(chatSummary, result.getBody().get(0));
    }

    @Test
    @DisplayName("Should reject getMyChats when user id is missing")
    void testGetMyChatsWithoutUserId() {
        assertThrows(AccessDeniedException.class, () -> chatController.getMyChats(null));
    }

    @Test
    @DisplayName("Should return specified chat details")
    void testGetChat() {
        ChatSummaryResponseDto chatSummary = new ChatSummaryResponseDto(
                2L,
                ChatType.GROUP,
                "Group chat",
                1L,
                LocalDateTime.now(),
                List.of(),
                null,
                1L
        );

        when(chatService.getChat(1L, 2L)).thenReturn(chatSummary);

        ResponseEntity<ChatSummaryResponseDto> result = chatController.getChat(1L, 2L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(chatSummary, result.getBody());
    }

    @Test
    @DisplayName("Should reject getChat when user id is missing")
    void testGetChatWithoutUserId() {
        assertThrows(AccessDeniedException.class, () -> chatController.getChat(null, 2L));
    }

    @Test
    @DisplayName("Should create or return direct chat")
    void testCreateOrGetDirectChat() {
        DirectChatRequestDto request = new DirectChatRequestDto(5L);
        ChatSummaryResponseDto chatSummary = new ChatSummaryResponseDto(
                3L,
                ChatType.DIRECT,
                "Direct chat with friend",
                1L,
                LocalDateTime.now(),
                List.of(),
                null,
                0L
        );

        when(chatService.createOrGetDirectChat(1L, 5L)).thenReturn(chatSummary);

        ResponseEntity<ChatSummaryResponseDto> result = chatController.createOrGetDirectChat(1L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(chatSummary, result.getBody());
    }

    @Test
    @DisplayName("Should reject createOrGetDirectChat when user id is missing")
    void testCreateOrGetDirectChatWithoutUserId() {
        DirectChatRequestDto request = new DirectChatRequestDto(5L);
        assertThrows(AccessDeniedException.class, () -> chatController.createOrGetDirectChat(null, request));
    }

    @Test
    @DisplayName("Should create group chat")
    void testCreateGroupChat() {
        GroupChatCreateRequestDto request = new GroupChatCreateRequestDto("Study group", Set.of(2L, 3L));
        ChatSummaryResponseDto chatSummary = new ChatSummaryResponseDto(
                4L,
                ChatType.GROUP,
                "Study group",
                1L,
                LocalDateTime.now(),
                List.of(),
                null,
                2L
        );

        when(chatService.createGroupChat(1L, request)).thenReturn(chatSummary);

        ResponseEntity<ChatSummaryResponseDto> result = chatController.createGroupChat(1L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(chatSummary, result.getBody());
    }

    @Test
    @DisplayName("Should reject createGroupChat when user id is missing")
    void testCreateGroupChatWithoutUserId() {
        GroupChatCreateRequestDto request = new GroupChatCreateRequestDto("Study group", Set.of(2L, 3L));
        assertThrows(AccessDeniedException.class, () -> chatController.createGroupChat(null, request));
    }

    @Test
    @DisplayName("Should return paged chat messages")
    void testGetMessages() {
        ChatMessageResponseDto message = new ChatMessageResponseDto(
                UUID.randomUUID(),
                10L,
                1L,
                MessageType.TEXT,
                "Hello",
                LocalDateTime.now()
        );

        Page<ChatMessageResponseDto> page = new PageImpl<>(List.of(message), PageRequest.of(0, 50), 1);
        when(chatService.getMessages(1L, 10L, 0, 50)).thenReturn(page);

        ResponseEntity<ChatMessagesPageResponseDto> result = chatController.getMessages(1L, 10L, 0, 50);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().content().size());
        assertEquals(0, result.getBody().page());
        assertEquals(50, result.getBody().size());
    }

    @Test
    @DisplayName("Should reject getMessages when user id is missing")
    void testGetMessagesWithoutUserId() {
        assertThrows(AccessDeniedException.class, () -> chatController.getMessages(null, 10L, 0, 50));
    }

    @Test
    @DisplayName("Should send chat message")
    void testSendMessage() {
        MessageSendRequestDto request = new MessageSendRequestDto(MessageType.TEXT, "Hello world");
        ChatMessageResponseDto response = new ChatMessageResponseDto(
                UUID.randomUUID(),
                10L,
                1L,
                MessageType.TEXT,
                "Hello world",
                LocalDateTime.now()
        );

        when(chatService.sendMessage(1L, 10L, request)).thenReturn(response);

        ResponseEntity<ChatMessageResponseDto> result = chatController.sendMessage(1L, 10L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    @DisplayName("Should reject sendMessage when user id is missing")
    void testSendMessageWithoutUserId() {
        MessageSendRequestDto request = new MessageSendRequestDto(MessageType.TEXT, "Hello world");
        assertThrows(AccessDeniedException.class, () -> chatController.sendMessage(null, 10L, request));
    }

    @Test
    @DisplayName("Should mark chat as read")
    void testMarkAsRead() {
        ChatSummaryResponseDto response = new ChatSummaryResponseDto(
                5L,
                ChatType.GROUP,
                "Book club",
                1L,
                LocalDateTime.now(),
                List.of(),
                null,
                0L
        );

        when(chatService.markAsRead(1L, 5L)).thenReturn(response);

        ResponseEntity<ChatSummaryResponseDto> result = chatController.markAsRead(1L, 5L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    @DisplayName("Should reject markAsRead when user id is missing")
    void testMarkAsReadWithoutUserId() {
        assertThrows(AccessDeniedException.class, () -> chatController.markAsRead(null, 5L));
    }
}
