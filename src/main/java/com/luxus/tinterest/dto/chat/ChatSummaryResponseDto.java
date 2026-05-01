package com.luxus.tinterest.dto.chat;

import com.luxus.tinterest.entity.ChatType;

import java.time.LocalDateTime;
import java.util.List;

public record ChatSummaryResponseDto(
        Long id,
        ChatType type,
        String title,
        Long createdBy,
        LocalDateTime createdAt,
        List<ChatMemberResponseDto> members,
        ChatMessageResponseDto lastMessage,
        long unreadCount
) {}
