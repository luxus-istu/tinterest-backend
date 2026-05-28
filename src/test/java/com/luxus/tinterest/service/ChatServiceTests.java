package com.luxus.tinterest.service;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService Unit Tests")
class ChatServiceTests {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ChatMemberRepository chatMemberRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatService chatService;

    @Test
    @DisplayName("Should throw when creating direct chat with yourself")
    void testCreateOrGetDirectChatThrowsOnSelfChat() {
        assertThrows(InvalidChatOperationException.class,
                () -> chatService.createOrGetDirectChat(1L, 1L));
    }

    @Test
    @DisplayName("Should throw when group chat member is not found")
    void testCreateGroupChatThrowsWhenMemberMissing() {
        User currentUser = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findAllById(Set.of(2L, 3L))).thenReturn(List.of(User.builder().id(2L).build()));

        GroupChatCreateRequestDto request = new GroupChatCreateRequestDto("Team", false, Set.of(2L, 3L));

        assertThrows(InvalidChatOperationException.class,
                () -> chatService.createGroupChat(1L, request));
    }

    @Test
    @DisplayName("Should throw when sending blank chat message")
    void testSendMessageThrowsWhenContentBlank() {
        MessageSendRequestDto request = new MessageSendRequestDto(null, "   ");

        Chat chat = Chat.builder().id(1L).build();
        when(chatRepository.findWithMembersById(1L)).thenReturn(Optional.of(chat));
        when(chatRepository.existsById(1L)).thenReturn(true);
        when(chatMemberRepository.findByChatIdAndUserId(1L, 1L)).thenReturn(Optional.of(ChatMember.builder().id(1L).chat(chat).user(User.builder().id(1L).build()).build()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));

        assertThrows(InvalidChatOperationException.class,
                () -> chatService.sendMessage(1L, 1L, request));
    }

    @Test
    @DisplayName("Should mark chat as read and return summary")
    void testMarkAsReadUpdatesLastReadAt() {
         User user = User.builder().id(1L).firstName("John").build();  // move up, chat needs it

        Chat chat = Chat.builder()
                .id(1L)
                .createdBy(user)  // add this
                .build();

        ChatMember member = ChatMember.builder()
                .id(10L)
                .chat(chat)
                .user(user)
                .role(ChatMemberRole.MEMBER)
                .build();
        chat.setMembers(Set.of(member));

        Message message = Message.builder().id(UUID.randomUUID()).chat(chat).sender(user).content("Hello").type(MessageType.TEXT).createdAt(LocalDateTime.now().minusMinutes(5)).build();

        when(chatRepository.findWithMembersById(1L)).thenReturn(Optional.of(chat));
        when(chatRepository.existsById(1L)).thenReturn(true);
        when(chatMemberRepository.findByChatIdAndUserId(1L, 1L)).thenReturn(Optional.of(member));
        when(messageRepository.findTopByChatIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(message));
        when(chatMemberRepository.save(member)).thenReturn(member);

        ChatSummaryResponseDto response = chatService.markAsRead(1L, 1L);

        assertEquals(1L, response.id());
        assertEquals(0, response.unreadCount());
        assertNotNull(member.getLastReadAt());
    }

    @Test
    @DisplayName("Should throw access denied when chat member is missing")
    void testGetChatThrowsWhenNotMember() {
        Chat chat = Chat.builder().id(1L).build();
        when(chatRepository.findWithMembersById(1L)).thenReturn(Optional.of(chat));
        when(chatRepository.existsById(1L)).thenReturn(true);
        when(chatMemberRepository.findByChatIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ChatAccessDeniedException.class,
                () -> chatService.getChat(1L, 1L));
    }

    @Test
    void createGroupChatRejectsSingleInvitedMember() {
        GroupChatCreateRequestDto request = new GroupChatCreateRequestDto("Small group", false, Set.of(2L));

        InvalidChatOperationException exception = assertThrows(
                InvalidChatOperationException.class,
                () -> chatService.createGroupChat(1L, request)
        );

        assertEquals("Group chat must include at least two other members", exception.getMessage());
        verifyNoInteractions(userRepository, chatRepository, chatMemberRepository, messageRepository);
    }

    @Test
    void createGroupChatDoesNotCountCurrentUserAsInvitedMember() {
        GroupChatCreateRequestDto request = new GroupChatCreateRequestDto("Small group", false, Set.of(1L, 2L));

        InvalidChatOperationException exception = assertThrows(
                InvalidChatOperationException.class,
                () -> chatService.createGroupChat(1L, request)
        );

        assertEquals("Group chat must include at least two other members", exception.getMessage());
        verifyNoInteractions(userRepository, chatRepository, chatMemberRepository, messageRepository);
    }
}
