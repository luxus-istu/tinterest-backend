package com.luxus.tinterest.dto.recommendation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SwipeResponse {
    private String result;
    private Long matchId;
    private Long chatId;
}
