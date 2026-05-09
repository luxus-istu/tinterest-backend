package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.interest.InterestResponseDto;
import com.luxus.tinterest.entity.Interest;
import com.luxus.tinterest.repository.InterestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InterestService Unit Tests")
class InterestServiceTests {

    @Mock
    private InterestRepository interestRepository;

    @InjectMocks
    private InterestService interestService;

    @Test
    @DisplayName("Should return interest responses ordered by name")
    void testGetInterestsReturnsResponses() {
        Interest first = Interest.builder().id(1L).name("Art").build();
        Interest second = Interest.builder().id(2L).name("Music").build();
        when(interestRepository.findAllByOrderByNameAsc()).thenReturn(List.of(first, second));

        var result = interestService.getInterests();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Art", result.get(0).name());
        assertEquals(2L, result.get(1).id());
        assertEquals("Music", result.get(1).name());
    }
}
