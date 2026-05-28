package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.interest.InterestResponseDto;
import com.luxus.tinterest.entity.Interest;
import com.luxus.tinterest.exception.admin.InterestAlreadyExistsException;
import com.luxus.tinterest.exception.admin.InterestNotFoundException;
import com.luxus.tinterest.repository.InterestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("Should add new interest and return response")
    void testAddInterestReturnsResponse() {
        String interestName = "Travel";
        Interest newInterest = Interest.builder().id(3L).name(interestName).build();
        when(interestRepository.existsByNameIgnoreCase(interestName)).thenReturn(false);
        when(interestRepository.save(any(Interest.class))).thenReturn(newInterest);

        var result = interestService.addInterest(interestName);

        assertEquals(3L, result.id());
        assertEquals("Travel", result.name());
    }   

    @Test
    @DisplayName("Should delete interest by id")
    void testDeleteInterest() {
        Long interestId = 1L;
        when(interestRepository.existsById(interestId)).thenReturn(true);

        interestService.deleteInterest(interestId);

        verify(interestRepository, times(1)).deleteById(interestId);
    }

    @Test
    @DisplayName("Should throw InterestAlreadyExistsException when interest name already exists")
    void testAddInterestThrowsInterestAlreadyExistsException() {
        String interestName = "Travel";
        when(interestRepository.existsByNameIgnoreCase(interestName)).thenReturn(true);

        assertThrows(InterestAlreadyExistsException.class, () -> 
            interestService.addInterest(interestName)
        );
        
        verify(interestRepository, never()).save(any(Interest.class));
    }

    @Test
    @DisplayName("Should throw InterestNotFoundException when deleting non-existent interest")
    void testDeleteInterestThrowsInterestNotFoundException() {
        Long interestId = 99L;
        when(interestRepository.existsById(interestId)).thenReturn(false);

        assertThrows(InterestNotFoundException.class, () -> 
            interestService.deleteInterest(interestId)
        );

        verify(interestRepository, never()).deleteById(anyLong());
    }
}