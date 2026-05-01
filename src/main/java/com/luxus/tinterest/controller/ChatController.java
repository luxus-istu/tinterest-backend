package com.luxus.tinterest.controller;

import com.luxus.tinterest.dto.chat.ChatMessageResponseDto;
import com.luxus.tinterest.dto.chat.ChatMessagesPageResponseDto;
import com.luxus.tinterest.dto.chat.ChatSummaryResponseDto;
import com.luxus.tinterest.dto.chat.DirectChatRequestDto;
import com.luxus.tinterest.dto.chat.GroupChatCreateRequestDto;
import com.luxus.tinterest.dto.chat.MessageSendRequestDto;
import com.luxus.tinterest.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<List<ChatSummaryResponseDto>> getMyChats(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(chatService.getMyChats(requireUserId(userId)));
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatSummaryResponseDto> getChat(@AuthenticationPrincipal Long userId,
                                                          @PathVariable Long chatId) {
        return ResponseEntity.ok(chatService.getChat(requireUserId(userId), chatId));
    }

    @PostMapping("/direct")
    public ResponseEntity<ChatSummaryResponseDto> createOrGetDirectChat(@AuthenticationPrincipal Long userId,
                                                                        @Valid @RequestBody DirectChatRequestDto request) {
        return ResponseEntity.ok(chatService.createOrGetDirectChat(requireUserId(userId), request.userId()));
    }

    @PostMapping("/groups")
    public ResponseEntity<ChatSummaryResponseDto> createGroupChat(@AuthenticationPrincipal Long userId,
                                                                  @Valid @RequestBody GroupChatCreateRequestDto request) {
        return ResponseEntity.ok(chatService.createGroupChat(requireUserId(userId), request));
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<ChatMessagesPageResponseDto> getMessages(@AuthenticationPrincipal Long userId,
                                                                   @PathVariable Long chatId,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "50") int size) {
        Page<ChatMessageResponseDto> messages = chatService.getMessages(requireUserId(userId), chatId, page, size);
        return ResponseEntity.ok(new ChatMessagesPageResponseDto(
                messages.getContent(),
                messages.getNumber(),
                messages.getSize(),
                messages.getTotalElements(),
                messages.getTotalPages()
        ));
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<ChatMessageResponseDto> sendMessage(@AuthenticationPrincipal Long userId,
                                                              @PathVariable Long chatId,
                                                              @Valid @RequestBody MessageSendRequestDto request) {
        return ResponseEntity.ok(chatService.sendMessage(requireUserId(userId), chatId, request));
    }

    @PostMapping("/{chatId}/read")
    public ResponseEntity<ChatSummaryResponseDto> markAsRead(@AuthenticationPrincipal Long userId,
                                                             @PathVariable Long chatId) {
        return ResponseEntity.ok(chatService.markAsRead(requireUserId(userId), chatId));
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new AccessDeniedException("Authenticated user is missing");
        }
        return userId;
    }
}
