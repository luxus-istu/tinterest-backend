package com.luxus.tinterest.controller;

import com.luxus.tinterest.dto.recommendation.*;
import com.luxus.tinterest.service.RecommendationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping("/v1/discovery")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RecommendationService recommendationService;


    @GetMapping("/recommendation")
    public ResponseEntity<RecommendationResponse> getRecommendations(@AuthenticationPrincipal Long userId, @RequestParam(defaultValue = "10") int limit) {
        log.info("Requesting recommendations for user: {}, limit: {}", userId, limit);
        return ResponseEntity.ok(
                recommendationService.getRecommendations(userId, limit)
        );
    }

    @GetMapping("/recommendation/filter")
    public ResponseEntity<FilteredRecommendationResponse> getRecommendationsWithFilters(
            @AuthenticationPrincipal Long userId,
            @ModelAttribute RecommendationFiltersDto filters,
            @RequestParam(defaultValue = "0")  @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int limit) {
        log.info("Requesting filtered recommendations for user: {}, filters: {}, page: {}, limit: {}", userId, filters, page, limit);
        return ResponseEntity.ok(
                recommendationService.getRecommendationsWithFilters(userId, filters, page, limit)
        );
    }

    @PostMapping("/swipe")
    public ResponseEntity<SwipeResponse> swipe(@AuthenticationPrincipal Long userId, @Valid @RequestBody SwipeRequest request) {
        log.info("User {} performed swipe on user {}: reaction {}", userId, request.getToUserId(), request.getReaction());
        return ResponseEntity.ok(
                recommendationService.swipe(userId, request)
        );
    }
}
