package com.luxus.tinterest.controller;

import com.luxus.tinterest.dto.recommendation.FilteredRecommendationResponse;
import com.luxus.tinterest.dto.recommendation.RecommendationFiltersDto;
import com.luxus.tinterest.dto.recommendation.RecommendationResponse;
import com.luxus.tinterest.dto.recommendation.SwipeRequest;
import com.luxus.tinterest.dto.recommendation.SwipeResponse;
import com.luxus.tinterest.dto.recommendation.UserCardDto;
import com.luxus.tinterest.enums.Reaction;
import com.luxus.tinterest.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Recommendation Controller Tests")
class RecommendationControllerTests {

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private RecommendationController recommendationController;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Should return recommendations for authenticated user")
    void testGetRecommendations() {
        RecommendationResponse response = RecommendationResponse.builder()
                .users(List.of(UserCardDto.builder().id(2L).firstName("Alice").build()))
                .hasMore(false)
                .cycle(1)
                .build();

        when(recommendationService.getRecommendations(1L, 10)).thenReturn(response);

        ResponseEntity<RecommendationResponse> result = recommendationController.getRecommendations(1L, 10);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    @DisplayName("Should forward null authenticated user id to recommendation service")
    void testGetRecommendationsNullUserId() {
        RecommendationResponse response = RecommendationResponse.builder()
                .users(List.of(UserCardDto.builder().id(2L).firstName("Alice").build()))
                .hasMore(false)
                .cycle(1)
                .build();

        when(recommendationService.getRecommendations(null, 10)).thenReturn(response);

        ResponseEntity<RecommendationResponse> result = recommendationController.getRecommendations(null, 10);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    @DisplayName("Should return filtered recommendations when no filter values are provided")
    void testGetRecommendationsWithEmptyFilters() {
        RecommendationFiltersDto filters = new RecommendationFiltersDto();

        FilteredRecommendationResponse response = FilteredRecommendationResponse.builder()
                .users(List.of(UserCardDto.builder().id(3L).firstName("Victor").build()))
                .hasMore(false)
                .empty(true)
                .build();

        when(recommendationService.getRecommendationsWithFilters(eq(1L), eq(filters), eq(0), eq(10)))
                .thenReturn(response);

        ResponseEntity<FilteredRecommendationResponse> result = recommendationController.getRecommendationsWithFilters(1L, filters, 0, 10);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    @DisplayName("Should return filtered recommendations with pagination")
    void testGetRecommendationsWithFilters() {
        RecommendationFiltersDto filters = new RecommendationFiltersDto();
        filters.setCity("Moscow");

        FilteredRecommendationResponse response = FilteredRecommendationResponse.builder()
                .users(List.of(UserCardDto.builder().id(3L).firstName("Victor").build()))
                .hasMore(true)
                .empty(false)
                .build();

        when(recommendationService.getRecommendationsWithFilters(eq(1L), any(RecommendationFiltersDto.class), eq(2), eq(5)))
                .thenReturn(response);

        ResponseEntity<FilteredRecommendationResponse> result = recommendationController.getRecommendationsWithFilters(1L, filters, 2, 5);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    @DisplayName("Should handle swipe request and return swipe result")
    void testSwipe() {
        SwipeRequest request = new SwipeRequest();
        request.setToUserId(4L);
        request.setReaction(Reaction.LIKE);

        SwipeResponse response = SwipeResponse.builder()
                .result("matched")
                .matchId(12L)
                .chatId(7L)
                .build();

        when(recommendationService.swipe(1L, request)).thenReturn(response);

        ResponseEntity<SwipeResponse> result = recommendationController.swipe(1L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }
}
