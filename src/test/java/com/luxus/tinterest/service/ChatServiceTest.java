package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.chat.GroupChatCreateRequestDto;
import com.luxus.tinterest.exception.chat.InvalidChatOperationException;
import com.luxus.tinterest.repository.ChatMemberRepository;
import com.luxus.tinterest.repository.ChatRepository;
import com.luxus.tinterest.repository.MessageRepository;
import com.luxus.tinterest.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

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

//    @Test
//    void createGroupChatRejectsSingleInvitedMember() {
//        GroupChatCreateRequestDto request = new GroupChatCreateRequestDto("Small group", Set.of(2L));
//
//        InvalidChatOperationException exception = assertThrows(
//                InvalidChatOperationException.class,
//                () -> chatService.createGroupChat(1L, request)
//        );
//
//        assertEquals("Group chat must include at least two other members", exception.getMessage());
//        verifyNoInteractions(userRepository, chatRepository, chatMemberRepository, messageRepository);
//    }
//
//    @Test
//    void createGroupChatDoesNotCountCurrentUserAsInvitedMember() {
//        GroupChatCreateRequestDto request = new GroupChatCreateRequestDto("Small group", Set.of(1L, 2L));
//
//        InvalidChatOperationException exception = assertThrows(
//                InvalidChatOperationException.class,
//                () -> chatService.createGroupChat(1L, request)
//        );
//
//        assertEquals("Group chat must include at least two other members", exception.getMessage());
//        verifyNoInteractions(userRepository, chatRepository, chatMemberRepository, messageRepository);
//    }
}
