package com.luxus.tinterest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxus.tinterest.dto.chat.ChatMessageResponseDto;
import com.luxus.tinterest.dto.chat.ChatSummaryResponseDto;
import com.luxus.tinterest.dto.chat.DirectChatRequestDto;
import com.luxus.tinterest.dto.chat.GroupChatCreateRequestDto;
import com.luxus.tinterest.dto.chat.MessageSendRequestDto;
import com.luxus.tinterest.entity.ChatType;
import com.luxus.tinterest.entity.MessageType;
import com.luxus.tinterest.exception.GlobalExceptionHandler;
import com.luxus.tinterest.exception.chat.ChatAccessDeniedException;
import com.luxus.tinterest.exception.chat.ChatNotFoundException;
import com.luxus.tinterest.exception.chat.InvalidChatOperationException;
import com.luxus.tinterest.exception.common.UserNotFoundException;
import com.luxus.tinterest.exception.handler.ChatHandler;
import com.luxus.tinterest.service.ChatService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Chat Controller Tests")
class ChatControllerTests {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController)
                .setControllerAdvice(new ChatHandler(), new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .defaultRequest(get("/").with(authorized()))
                .addFilters(new AuthorizationHeaderFilter())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private void mockAuthenticationPrincipal(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private RequestPostProcessor authorized() {
        return request -> {
            request.addHeader("Authorization", "Bearer dummy-token");
            return request;
        };
    }

    private RequestPostProcessor unauthorized() {
        return request -> {
            request.removeHeader("Authorization");
            return request;
        };
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private ChatSummaryResponseDto createSampleChat(Long chatId) {
        return new ChatSummaryResponseDto(
            chatId,
            ChatType.DIRECT,
            "Chat Title",
            false,
            1L,
            LocalDateTime.now(),
            List.of(),
            null,
            5L
        );
    }

    // -------------------------------------------------------------------------
    // GET /v1/chats - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully retrieve user's chats")
    void testGetChatsSuccess() throws Exception {
        mockAuthenticationPrincipal(1L);
        ChatSummaryResponseDto chat1 = createSampleChat(1L);
        ChatSummaryResponseDto chat2 = createSampleChat(2L);
        when(chatService.getMyChats(1L)).thenReturn(Arrays.asList(chat1, chat2));

        mockMvc.perform(get("/v1/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Chat Title"));
    }

    @Test
    @DisplayName("Should return empty list when user has no chats")
    void testGetChatsEmpty() throws Exception {
        mockAuthenticationPrincipal(1L);
        when(chatService.getMyChats(1L)).thenReturn(List.of());

        mockMvc.perform(get("/v1/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // -------------------------------------------------------------------------
    // GET /v1/chats - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 404 when user not found")
    void testGetChatsUserNotFound() throws Exception {
        mockAuthenticationPrincipal(999L);
        doThrow(new UserNotFoundException()).when(chatService).getMyChats(999L);

        mockMvc.perform(get("/v1/chats"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when retrieving chats without authentication")
    void testGetChatsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v1/chats").with(unauthorized()))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // GET /v1/chats/{chatId} - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully retrieve chat by id")
    void testGetChatSuccess() throws Exception {
        mockAuthenticationPrincipal(1L);
        ChatSummaryResponseDto chat = createSampleChat(1L);
        when(chatService.getChat(1L, 1L)).thenReturn(chat);

        mockMvc.perform(get("/v1/chats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Chat Title"));
    }

    // -------------------------------------------------------------------------
    // GET /v1/chats/{chatId} - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 404 when chat not found")
    void testGetChatNotFound() throws Exception {
        mockAuthenticationPrincipal(1L);
        doThrow(new ChatNotFoundException()).when(chatService).getChat(1L, 999L);

        mockMvc.perform(get("/v1/chats/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 403 when user does not have access to chat")
    void testGetChatAccessDenied() throws Exception {
        mockAuthenticationPrincipal(1L);
        doThrow(new ChatAccessDeniedException()).when(chatService).getChat(1L, 1L);

        mockMvc.perform(get("/v1/chats/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 when retrieving chat by id without authentication")
    void testGetChatWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v1/chats/1").with(unauthorized()))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // POST /v1/chats/direct - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully create direct chat")
    void testCreateDirectChatSuccess() throws Exception {
        mockAuthenticationPrincipal(1L);
        DirectChatRequestDto request = new DirectChatRequestDto(2L);
        ChatSummaryResponseDto response = createSampleChat(1L);
        when(chatService.createOrGetDirectChat(1L, 2L)).thenReturn(response);

        mockMvc.perform(post("/v1/chats/direct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // -------------------------------------------------------------------------
    // POST /v1/chats/direct - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 400 when recipientId is missing")
    void testCreateDirectChatWithoutRecipient() throws Exception {
        DirectChatRequestDto request = new DirectChatRequestDto(null);

        mockMvc.perform(post("/v1/chats/direct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when recipient user not found")
    void testCreateDirectChatRecipientNotFound() throws Exception {
        mockAuthenticationPrincipal(1L);
        DirectChatRequestDto request = new DirectChatRequestDto(999L);
        doThrow(new UserNotFoundException()).when(chatService).createOrGetDirectChat(1L, 999L);

        mockMvc.perform(post("/v1/chats/direct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 for invalid chat operation")
    void testCreateDirectChatInvalidOperation() throws Exception {
        mockAuthenticationPrincipal(1L);
        DirectChatRequestDto request = new DirectChatRequestDto(2L);
        doThrow(new InvalidChatOperationException("Invalid chat operation")).when(chatService).createOrGetDirectChat(1L, 2L);

        mockMvc.perform(post("/v1/chats/direct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 when creating direct chat without authentication")
    void testCreateDirectChatWithoutAuthentication() throws Exception {
        DirectChatRequestDto request = new DirectChatRequestDto(2L);

        mockMvc.perform(post("/v1/chats/direct").with(unauthorized())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // POST /v1/chats/groups - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully create group chat")
    void testCreateGroupChatSuccess() throws Exception {
        mockAuthenticationPrincipal(1L);
        GroupChatCreateRequestDto request = new GroupChatCreateRequestDto(
                "Friends Group", false, Set.of(2L, 3L)
        );
        ChatSummaryResponseDto response = createSampleChat(1L);
        when(chatService.createGroupChat(eq(1L), any(GroupChatCreateRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/v1/chats/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // -------------------------------------------------------------------------
    // POST /v1/chats/groups - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 400 when group name is missing")
    void testCreateGroupChatWithoutName() throws Exception {
        mockAuthenticationPrincipal(1L);
        GroupChatCreateRequestDto request = new GroupChatCreateRequestDto(
                null, false, Set.of(2L, 3L)
        );

        mockMvc.perform(post("/v1/chats/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when member list is empty")
    void testCreateGroupChatNoMembers() throws Exception {
        mockAuthenticationPrincipal(1L);
        GroupChatCreateRequestDto request = new GroupChatCreateRequestDto(
                "Friends Group", false, Set.of()
        );

        mockMvc.perform(post("/v1/chats/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 when creating group chat without authentication")
    void testCreateGroupChatWithoutAuthentication() throws Exception {
        GroupChatCreateRequestDto request = new GroupChatCreateRequestDto(
                "Friends Group", false, Set.of(2L, 3L)
        );

        mockMvc.perform(post("/v1/chats/groups").with(unauthorized())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // GET /v1/chats/{chatId}/messages - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully retrieve chat messages")
    void testGetChatMessagesSuccess() throws Exception {
        mockAuthenticationPrincipal(1L);
        ChatMessageResponseDto msg1 = new ChatMessageResponseDto(UUID.randomUUID(), 1L, 1L, MessageType.TEXT, "Hello", LocalDateTime.now());
        ChatMessageResponseDto msg2 = new ChatMessageResponseDto(UUID.randomUUID(), 1L, 2L, MessageType.TEXT, "Hi there", LocalDateTime.now());
        Page<ChatMessageResponseDto> page = new PageImpl<>(Arrays.asList(msg1, msg2), PageRequest.of(0, 10), 2);

        when(chatService.getMessages(1L, 1L, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/v1/chats/1/messages")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Should return empty page when no messages in chat")
    void testGetChatMessagesEmpty() throws Exception {
        mockAuthenticationPrincipal(1L);
        Page<ChatMessageResponseDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(chatService.getMessages(1L, 1L, 0, 10)).thenReturn(emptyPage);

        mockMvc.perform(get("/v1/chats/1/messages")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // -------------------------------------------------------------------------
    // GET /v1/chats/{chatId}/messages - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 404 when chat not found for messages")
    void testGetChatMessagesNotFound() throws Exception {
        mockAuthenticationPrincipal(1L);
        doThrow(new ChatNotFoundException()).when(chatService).getMessages(anyLong(), anyLong(), anyInt(), anyInt());

        mockMvc.perform(get("/v1/chats/999/messages"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 403 when no access to chat messages")
    void testGetChatMessagesAccessDenied() throws Exception {
        mockAuthenticationPrincipal(1L);
        doThrow(new ChatAccessDeniedException()).when(chatService).getMessages(anyLong(), anyLong(), anyInt(), anyInt());

        mockMvc.perform(get("/v1/chats/1/messages"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 when retrieving chat messages without authentication")
    void testGetChatMessagesWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v1/chats/1/messages").with(unauthorized())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // POST /v1/chats/{chatId}/messages - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully send message")
    void testSendMessageSuccess() throws Exception {
        mockAuthenticationPrincipal(1L);

        UUID uuidResponse = UUID.randomUUID();
        MessageSendRequestDto request = new MessageSendRequestDto(MessageType.TEXT, "Hello everyone!");
        ChatMessageResponseDto response = new ChatMessageResponseDto(
                uuidResponse, 1L, 1L, MessageType.TEXT, "Hello everyone!", LocalDateTime.now()
        );
        when(chatService.sendMessage(eq(1L), eq(1L), any(MessageSendRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/v1/chats/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(uuidResponse.toString()))
                .andExpect(jsonPath("$.content").value("Hello everyone!"));
    }

    // -------------------------------------------------------------------------
    // POST /v1/chats/{chatId}/messages - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 400 when message content is missing")
    void testSendMessageWithoutContent() throws Exception {
        mockAuthenticationPrincipal(1L);
        MessageSendRequestDto request = new MessageSendRequestDto(MessageType.TEXT, null);

        mockMvc.perform(post("/v1/chats/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when message content is empty")
    void testSendMessageEmptyContent() throws Exception {
        mockAuthenticationPrincipal(1L);
        MessageSendRequestDto request = new MessageSendRequestDto(MessageType.TEXT, "");

        mockMvc.perform(post("/v1/chats/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when chat not found for message send")
    void testSendMessageChatNotFound() throws Exception {
        mockAuthenticationPrincipal(1L);
        MessageSendRequestDto request = new MessageSendRequestDto(MessageType.TEXT, "Hello");
        doThrow(new ChatNotFoundException()).when(chatService).sendMessage(eq(1L), eq(999L), any(MessageSendRequestDto.class));

        mockMvc.perform(post("/v1/chats/999/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 403 when no access to send message")
    void testSendMessageAccessDenied() throws Exception {
        mockAuthenticationPrincipal(1L);
        MessageSendRequestDto request = new MessageSendRequestDto(MessageType.TEXT, "Hello");
        doThrow(new ChatAccessDeniedException()).when(chatService).sendMessage(eq(1L), eq(1L), any(MessageSendRequestDto.class));

        mockMvc.perform(post("/v1/chats/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 when sending message without authentication")
    void testSendMessageWithoutAuthentication() throws Exception {
        MessageSendRequestDto request = new MessageSendRequestDto(MessageType.TEXT, "Hello everyone!");

        mockMvc.perform(post("/v1/chats/1/messages").with(unauthorized())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // POST /v1/chats/{chatId}/read - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully mark chat as read")
    void testMarkChatReadSuccess() throws Exception {
        mockAuthenticationPrincipal(1L);
        ChatSummaryResponseDto response = createSampleChat(1L);
        when(chatService.markAsRead(eq(1L), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/v1/chats/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // -------------------------------------------------------------------------
    // POST /v1/chats/{chatId}/read - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 404 when chat not found for marking as read")
    void testMarkChatReadNotFound() throws Exception {
        mockAuthenticationPrincipal(1L);
        doThrow(new ChatNotFoundException()).when(chatService).markAsRead(eq(1L), eq(999L));

        mockMvc.perform(post("/v1/chats/999/read"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 403 when no access to mark chat as read")
    void testMarkChatReadAccessDenied() throws Exception {
        mockAuthenticationPrincipal(1L);
        doThrow(new ChatAccessDeniedException()).when(chatService).markAsRead(eq(1L), eq(1L));

        mockMvc.perform(post("/v1/chats/1/read"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 when marking chat as read without authentication")
    void testMarkChatReadWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/v1/chats/1/read").with(unauthorized()))
                .andExpect(status().isUnauthorized());
    }

    private static final class AuthorizationHeaderFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            if (httpRequest.getHeader("Authorization") == null) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            chain.doFilter(request, response);
        }
    }
}
