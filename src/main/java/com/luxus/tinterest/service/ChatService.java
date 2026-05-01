package com.luxus.tinterest.service;

import com.luxus.tinterest.entity.Chat;
import com.luxus.tinterest.entity.ChatMember;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.enums.ChatMemberRole;
import com.luxus.tinterest.enums.ChatType;
import com.luxus.tinterest.repository.ChatMemberRepository;
import com.luxus.tinterest.repository.ChatRepository;
import com.luxus.tinterest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final UserRepository userRepository;

    public Long createPrivateChat(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Chat chat = new Chat();
        chat.setType(ChatType.PRIVATE);
        chat.setCreatedBy(null);
        chatRepository.save(chat);


        ChatMember member1 = new ChatMember();
        member1.setChatId(chat.getId());
        member1.setUserId(userId1);
        member1.setRole(ChatMemberRole.MEMBER);
        chatMemberRepository.save(member1);

        ChatMember member2 = new ChatMember();
        member2.setChatId(chat.getId());
        member2.setUserId(userId2);
        member2.setRole(ChatMemberRole.MEMBER);
        chatMemberRepository.save(member2);

        return chat.getId();
    }
}
