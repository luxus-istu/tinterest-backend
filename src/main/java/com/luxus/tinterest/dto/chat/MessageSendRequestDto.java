package com.luxus.tinterest.dto.chat;

import com.luxus.tinterest.entity.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageSendRequestDto(
        MessageType type,

        @NotBlank(message = "Message content is required")
        @Size(max = 4000, message = "Message content must be at most 4000 characters")
        String content
) {}
