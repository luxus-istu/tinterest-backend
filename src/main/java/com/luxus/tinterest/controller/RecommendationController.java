package com.luxus.tinterest.controller;

import com.luxus.tinterest.dto.recommendation.*;
import com.luxus.tinterest.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/recommendation")
    public ResponseEntity<RecommendationResponse> getRecommendations(@AuthenticationPrincipal Long userId, @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(
                recommendationService.getRecommendations(userId, limit)
        );
    }

    @GetMapping("/recommendation/filter")
    public ResponseEntity<FilteredRecommendationResponse> getRecommendationsWithFilters(
            @AuthenticationPrincipal Long userId,
            @ModelAttribute RecommendationFiltersDto filters,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(
                recommendationService.getRecommendationsWithFilters(userId, filters, page, limit)
        );
    }

    @PostMapping("/swipe")
    public ResponseEntity<SwipeResponse> swipe(@AuthenticationPrincipal Long userId, @RequestBody SwipeRequest request) {
        return ResponseEntity.ok(
                recommendationService.swipe(userId, request)
        );
    }
}
