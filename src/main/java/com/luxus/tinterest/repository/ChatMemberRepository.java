package com.luxus.tinterest.repository;

import com.luxus.tinterest.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    boolean existsByChatIdAndUserId(Long chatId, Long userId);

    Optional<ChatMember> findByChatIdAndUserId(Long chatId, Long userId);
}
