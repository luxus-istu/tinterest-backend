package com.luxus.tinterest.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserSummaryResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String avatarUrl;
    private boolean blocked;
    private Instant createdAt;
}
