package com.luxus.tinterest.dto.recommendation;

import com.luxus.tinterest.enums.Reaction;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SwipeRequest {

    @NotNull(message = "toUserId is required")
    private Long toUserId;

    @NotNull(message = "reaction is required")
    private Reaction reaction;
}
