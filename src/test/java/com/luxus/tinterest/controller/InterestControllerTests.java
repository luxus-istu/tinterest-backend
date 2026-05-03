package com.luxus.tinterest.controller;

import com.luxus.tinterest.dto.interest.InterestResponseDto;
import com.luxus.tinterest.service.InterestService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Interest Controller Tests")
class InterestControllerTests {

    @Mock
    private InterestService interestService;

    @InjectMocks
    private InterestController interestController;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Should return available interests")
    void testGetInterests() {
        List<InterestResponseDto> interests = List.of(
                new InterestResponseDto(1L, "Gaming"),
                new InterestResponseDto(2L, "Cooking")
        );

        when(interestService.getInterests()).thenReturn(interests);

        ResponseEntity<List<InterestResponseDto>> result = interestController.getInterests();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(interests, result.getBody());
    }

    @Test
    @DisplayName("Should return empty interest list when no interests exist")
    void testGetInterestsEmpty() {
        when(interestService.getInterests()).thenReturn(List.of());

        ResponseEntity<List<InterestResponseDto>> result = interestController.getInterests();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(List.of(), result.getBody());
    }
}
