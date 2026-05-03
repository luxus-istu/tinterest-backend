package com.luxus.tinterest.dto.chat;

import com.luxus.tinterest.entity.MessageType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageResponseDto(
        UUID id,
        Long chatId,
        Long senderId,
        MessageType type,
        String content,
        LocalDateTime createdAt
) {}
