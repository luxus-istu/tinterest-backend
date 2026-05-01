package com.luxus.tinterest.dto.chat;

import jakarta.validation.constraints.NotNull;

public record DirectChatRequestDto(
        @NotNull(message = "User id is required")
        Long userId
) {}
