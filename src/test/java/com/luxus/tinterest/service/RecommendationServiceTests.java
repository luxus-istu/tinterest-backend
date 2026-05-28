package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.chat.ChatSummaryResponseDto;
import com.luxus.tinterest.dto.recommendation.FilteredRecommendationResponse;
import com.luxus.tinterest.dto.recommendation.RecommendationFiltersDto;
import com.luxus.tinterest.dto.recommendation.RecommendationResponse;
import com.luxus.tinterest.dto.recommendation.SwipeRequest;
import com.luxus.tinterest.dto.recommendation.SwipeResponse;
import com.luxus.tinterest.entity.Match;
import com.luxus.tinterest.entity.RecommendationOffset;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.enums.Reaction;
import com.luxus.tinterest.repository.MatchRepository;
import com.luxus.tinterest.repository.RecommendationOffsetRepository;
import com.luxus.tinterest.repository.RecommendationRepository;
import com.luxus.tinterest.repository.UserRepository;
import com.luxus.tinterest.repository.UserSwipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationService Unit Tests")
class RecommendationServiceTests {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private RecommendationOffsetRepository offsetRepository;

    @Mock
    private UserSwipeRepository swipeRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatService chatService;

    @Mock
    private RedisTemplate<String, String> redis;

    @Mock
    private ListOperations<String, String> listOperations;

    @InjectMocks
    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Should build recommendations and return ordered user cards")
    void testGetRecommendationsBuildsCache() {
        when(redis.opsForList()).thenReturn(listOperations);


        Long userId = 1L;
        String key = "recommendation:" + userId;

        when(offsetRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(offsetRepository.save(any(RecommendationOffset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(redis.hasKey(key)).thenReturn(false);
        when(recommendationRepository.computeSortedFeedIds(userId)).thenReturn(List.of(2L, 3L));
        when(listOperations.size(key)).thenReturn(2L);
        when(listOperations.range(key, 0, 1)).thenReturn(List.of("2", "3"));

        User secondUser = User.builder().id(2L).firstName("Anna").build();
        User thirdUser = User.builder().id(3L).firstName("Boris").build();
        when(userRepository.findAllByIdWithInterests(List.of(2L, 3L))).thenReturn(List.of(secondUser, thirdUser));

        RecommendationResponse response = recommendationService.getRecommendations(userId, 2);

        assertEquals(2, response.getUsers().size());
        assertEquals(2L, response.getUsers().get(0).getId());
        assertTrue(response.isHasMore());
        verify(offsetRepository).updateOffsetAndCycle(userId, 2, 0);
        verify(redis).expire(key, 1L, java.util.concurrent.TimeUnit.HOURS);
    }

    @Test
    @DisplayName("Should return filtered recommendations and set empty flag")
    void testGetRecommendationsWithFiltersReturnsEmpty() {
        Long userId = 1L;
        RecommendationFiltersDto filters = new RecommendationFiltersDto();
        filters.setCity("Berlin");

        when(recommendationRepository.findWithFilters(userId, filters, 10, 0)).thenReturn(List.of());

        FilteredRecommendationResponse response = recommendationService.getRecommendationsWithFilters(userId, filters, 0, 10);

        assertTrue(response.isEmpty());
        assertFalse(response.isHasMore());
        assertTrue(response.getUsers().isEmpty());
    }

    @Test
    @DisplayName("Should return liked swipe result when no match is created")
    void testSwipeReturnsLikedWhenNoMutualMatch() {
        SwipeRequest request = new SwipeRequest();
        request.setToUserId(2L);
        request.setReaction(Reaction.LIKE);

        when(swipeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(swipeRepository.existsByFromUserIdAndToUserIdAndReaction(2L, 1L, Reaction.LIKE)).thenReturn(false);

        SwipeResponse response = recommendationService.swipe(1L, request);

        assertEquals("LIKED", response.getResult());
        assertNull(response.getMatchId());
        verify(matchRepository, never()).save(any());
        verify(chatService, never()).createOrGetDirectChat(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Should create match and chat when swipe is mutual like")
    void testSwipeCreatesMatchWhenMutualLike() {
        SwipeRequest request = new SwipeRequest();
        request.setToUserId(2L);
        request.setReaction(Reaction.LIKE);

        when(swipeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(swipeRepository.existsByFromUserIdAndToUserIdAndReaction(2L, 1L, Reaction.LIKE)).thenReturn(true);

        Match savedMatch = new Match();
        savedMatch.setId(10L);
        when(matchRepository.save(any())).thenReturn(savedMatch);

        ChatSummaryResponseDto chatSummary = new ChatSummaryResponseDto(5L, null, null, 1L, null, List.of(), null, 0);
        when(chatService.createOrGetDirectChat(1L, 2L)).thenReturn(chatSummary);

        SwipeResponse response = recommendationService.swipe(1L, request);

        assertEquals("MATCHED", response.getResult());
        assertEquals(10L, response.getMatchId());
        assertEquals(5L, response.getChatId());
        verify(redis).delete("recommendation:1");
        verify(redis).delete("recommendation:2");


        // Match match = new Match();
        // match.setUserId1(fromUserId);
        // match.setUserId2(toUserId);
        // Match savedMatch = matchRepository.save(match);

        // ChatSummaryResponseDto chatDto = chatService.createOrGetDirectChat(fromUserId, toUserId);

        // redis.delete(RECOMMENDATION_KEY + fromUserId);
        // redis.delete(RECOMMENDATION_KEY + toUserId);

        // return SwipeResponse.builder()
        //         .result("MATCHED")
        //         .matchId(savedMatch.getId())
        //         .chatId(chatDto.id())
        //         .build();

        // По сути все работает, просто тест не проходит
        // этот код нужно в RecommendationService в конце вставить вместо старого чтобы тест пофиксить.
        // - QA
    }
}
