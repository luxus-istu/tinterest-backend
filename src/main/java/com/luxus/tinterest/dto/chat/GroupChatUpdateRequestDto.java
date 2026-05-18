package com.luxus.tinterest.dto.chat;

import jakarta.validation.constraints.Size;

public record GroupChatUpdateRequestDto(
        @Size(max = 120, message = "Title must be at most 120 characters")
        String title,

        Boolean isPublic
) {}
