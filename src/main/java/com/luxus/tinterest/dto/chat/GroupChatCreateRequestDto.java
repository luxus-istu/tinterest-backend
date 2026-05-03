package com.luxus.tinterest.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record GroupChatCreateRequestDto(
        @NotBlank(message = "Title is required")
        @Size(max = 120, message = "Title must be at most 120 characters")
        String title,

        @NotEmpty(message = "Member ids are required")
        Set<Long> memberIds
) {}
