package com.luxus.tinterest.dto.recommendation;

import com.luxus.tinterest.enums.Reaction;
import lombok.Data;

@Data
public class SwipeRequest {
    private Long toUserId;
    private Reaction reaction;
}
