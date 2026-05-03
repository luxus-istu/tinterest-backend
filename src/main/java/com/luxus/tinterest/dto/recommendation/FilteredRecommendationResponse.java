package com.luxus.tinterest.dto.recommendation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FilteredRecommendationResponse {
    private List<UserCardDto> users;
    private boolean hasMore;
    private boolean empty;
}
