package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.chat.ChatMemberResponseDto;
import com.luxus.tinterest.dto.chat.ChatMessageResponseDto;
import com.luxus.tinterest.dto.chat.ChatSummaryResponseDto;
import com.luxus.tinterest.dto.chat.GroupChatCreateRequestDto;
import com.luxus.tinterest.dto.chat.MessageSendRequestDto;
import com.luxus.tinterest.entity.Chat;
import com.luxus.tinterest.entity.ChatMember;
import com.luxus.tinterest.entity.ChatMemberRole;
import com.luxus.tinterest.entity.ChatType;
import com.luxus.tinterest.entity.Message;
import com.luxus.tinterest.entity.MessageType;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.exception.chat.ChatAccessDeniedException;
import com.luxus.tinterest.exception.chat.ChatNotFoundException;
import com.luxus.tinterest.exception.chat.InvalidChatOperationException;
import com.luxus.tinterest.exception.common.UserNotFoundException;
import com.luxus.tinterest.repository.ChatMemberRepository;
import com.luxus.tinterest.repository.ChatRepository;
import com.luxus.tinterest.repository.MessageRepository;
import com.luxus.tinterest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int MAX_MESSAGE_PAGE_SIZE = 100;
    private static final LocalDateTime UNREAD_COUNTER_START = LocalDateTime.of(1970, 1, 1, 0, 0);

    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public List<ChatSummaryResponseDto> getMyChats(Long userId) {
        return chatRepository.findDistinctByMembers_User_IdOrderByCreatedAtDesc(userId).stream()
                .map(chat -> toChatSummary(chat, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatSummaryResponseDto getChat(Long userId, Long chatId) {
        Chat chat = loadChatWithMembers(chatId);
        requireMember(chatId, userId);
        return toChatSummary(chat, userId);
    }

    @Transactional
    public ChatSummaryResponseDto createOrGetDirectChat(Long currentUserId, Long otherUserId) {
        if (currentUserId.equals(otherUserId)) {
            throw new InvalidChatOperationException("Direct chat with yourself is not allowed");
        }

        User currentUser = userRepository.findById(currentUserId).orElseThrow(UserNotFoundException::new);
        User otherUser = userRepository.findById(otherUserId).orElseThrow(UserNotFoundException::new);

        return chatRepository.findDirectChatBetween(ChatType.DIRECT, currentUserId, otherUserId)
                .map(chat -> toChatSummary(chat, currentUserId))
                .orElseGet(() -> {
                    Chat chat = chatRepository.save(Chat.builder()
                            .type(ChatType.DIRECT)
                            .createdBy(currentUser)
                            .build());

                    chatMemberRepository.save(ChatMember.builder()
                            .chat(chat)
                            .user(currentUser)
                            .role(ChatMemberRole.OWNER)
                            .lastReadAt(LocalDateTime.now())
                            .build());
                    chatMemberRepository.save(ChatMember.builder()
                            .chat(chat)
                            .user(otherUser)
                            .role(ChatMemberRole.MEMBER)
                            .build());

                    return toChatSummary(loadChatWithMembers(chat.getId()), currentUserId);
                });
    }

    @Transactional
    public ChatSummaryResponseDto createGroupChat(Long currentUserId, GroupChatCreateRequestDto request) {
        User currentUser = userRepository.findById(currentUserId).orElseThrow(UserNotFoundException::new);
        Set<Long> memberIds = new LinkedHashSet<>(request.memberIds());
        memberIds.remove(currentUserId);

        List<User> invitedUsers = userRepository.findAllById(memberIds);
        if (invitedUsers.size() != memberIds.size()) {
            throw new InvalidChatOperationException("One or more chat members were not found");
        }

        Chat chat = chatRepository.save(Chat.builder()
                .type(ChatType.GROUP)
                .title(cleanValue(request.title()))
                .createdBy(currentUser)
                .build());

        chatMemberRepository.save(ChatMember.builder()
                .chat(chat)
                .user(currentUser)
                .role(ChatMemberRole.OWNER)
                .lastReadAt(LocalDateTime.now())
                .build());

        invitedUsers.forEach(user -> chatMemberRepository.save(ChatMember.builder()
                .chat(chat)
                .user(user)
                .role(ChatMemberRole.MEMBER)
                .build()));

        return toChatSummary(loadChatWithMembers(chat.getId()), currentUserId);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponseDto> getMessages(Long userId, Long chatId, int page, int size) {
        requireMember(chatId, userId);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_MESSAGE_PAGE_SIZE));
        return messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable).map(this::toMessageResponse);
    }

    @Transactional
    public ChatMessageResponseDto sendMessage(Long userId, Long chatId, MessageSendRequestDto request) {
        Chat chat = loadChatWithMembers(chatId);
        ChatMember senderMember = requireMember(chatId, userId);
        User sender = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .type(request.type() == null ? MessageType.TEXT : request.type())
                .content(cleanContent(request.content()))
                .build();

        Message savedMessage = messageRepository.saveAndFlush(message);
        senderMember.setLastReadAt(savedMessage.getCreatedAt());
        chatMemberRepository.save(senderMember);

        ChatMessageResponseDto response = toMessageResponse(savedMessage);
        messagingTemplate.convertAndSend("/topic/chats/" + chatId, response);
        return response;
    }

    @Transactional
    public ChatSummaryResponseDto markAsRead(Long userId, Long chatId) {
        Chat chat = loadChatWithMembers(chatId);
        ChatMember member = requireMember(chatId, userId);
        LocalDateTime lastMessageCreatedAt = messageRepository.findTopByChatIdOrderByCreatedAtDesc(chatId)
                .map(Message::getCreatedAt)
                .orElse(LocalDateTime.now());
        member.setLastReadAt(lastMessageCreatedAt);
        chatMemberRepository.save(member);
        return toChatSummary(chat, userId);
    }

    private Chat loadChatWithMembers(Long chatId) {
        return chatRepository.findWithMembersById(chatId).orElseThrow(ChatNotFoundException::new);
    }

    private ChatMember requireMember(Long chatId, Long userId) {
        if (!chatRepository.existsById(chatId)) {
            throw new ChatNotFoundException();
        }
        return chatMemberRepository.findByChatIdAndUserId(chatId, userId).orElseThrow(ChatAccessDeniedException::new);
    }

    private ChatSummaryResponseDto toChatSummary(Chat chat, Long viewerId) {
        ChatMessageResponseDto lastMessage = messageRepository.findTopByChatIdOrderByCreatedAtDesc(chat.getId())
                .map(this::toMessageResponse)
                .orElse(null);

        LocalDateTime lastReadAt = chat.getMembers().stream()
                .filter(member -> member.getUser().getId().equals(viewerId))
                .findFirst()
                .map(ChatMember::getLastReadAt)
                .orElse(null);

        long unreadCount = messageRepository.countByChatIdAndCreatedAtAfterAndSenderIdNot(
                chat.getId(),
                lastReadAt == null ? UNREAD_COUNTER_START : lastReadAt,
                viewerId
        );

        return new ChatSummaryResponseDto(
                chat.getId(),
                chat.getType(),
                chat.getTitle(),
                chat.getCreatedBy().getId(),
                chat.getCreatedAt(),
                chat.getMembers().stream()
                        .map(this::toMemberResponse)
                        .toList(),
                lastMessage,
                unreadCount
        );
    }

    private ChatMemberResponseDto toMemberResponse(ChatMember member) {
        User user = member.getUser();
        return new ChatMemberResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl(),
                member.getRole(),
                member.getJoinedAt(),
                member.getLastReadAt()
        );
    }

    private ChatMessageResponseDto toMessageResponse(Message message) {
        return new ChatMessageResponseDto(
                message.getId(),
                message.getChat().getId(),
                message.getSender().getId(),
                message.getType(),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    private String cleanContent(String content) {
        String cleaned = cleanValue(content);
        if (cleaned == null || cleaned.isBlank()) {
            throw new InvalidChatOperationException("Message content must not be blank");
        }
        if (cleaned.length() > 4000) {
            throw new InvalidChatOperationException("Message content must be at most 4000 characters");
        }
        return cleaned;
    }

    private String cleanValue(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }
}
