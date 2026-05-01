package com.luxus.tinterest.dto.chat;

import java.util.List;

public record ChatMessagesPageResponseDto(
        List<ChatMessageResponseDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
