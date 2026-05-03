package com.luxus.tinterest.dto.chat;

import com.luxus.tinterest.entity.ChatMemberRole;

import java.time.LocalDateTime;

public record ChatMemberResponseDto(
        Long userId,
        String firstName,
        String lastName,
        String avatarUrl,
        ChatMemberRole role,
        LocalDateTime joinedAt,
        LocalDateTime lastReadAt
) {}
