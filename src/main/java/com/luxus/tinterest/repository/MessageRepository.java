package com.luxus.tinterest.repository;

import com.luxus.tinterest.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @EntityGraph(attributePaths = {"sender"})
    Page<Message> findByChatIdOrderByCreatedAtDesc(Long chatId, Pageable pageable);

    @EntityGraph(attributePaths = {"sender"})
    Optional<Message> findTopByChatIdOrderByCreatedAtDesc(Long chatId);

    long countByChatIdAndCreatedAtAfterAndSenderIdNot(Long chatId, LocalDateTime createdAt, Long senderId);
}
