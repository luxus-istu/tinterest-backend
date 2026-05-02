package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.chat.ChatSummaryResponseDto;
import com.luxus.tinterest.dto.recommendation.*;
import com.luxus.tinterest.entity.Match;
import com.luxus.tinterest.entity.RecommendationOffset;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.entity.UserSwipe;
import com.luxus.tinterest.enums.Reaction;
import com.luxus.tinterest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final RecommendationOffsetRepository offsetRepository;
    private final UserSwipeRepository swipeRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final RedisTemplate<String, String> redis;

    private static final String RECOMMENDATION_KEY = "recommendation:";
    private static final long TTL_HOURS = 1;


    @Transactional
    public RecommendationResponse getRecommendations(Long userId, int limit) {
        String key = RECOMMENDATION_KEY + userId;

        RecommendationOffset offsetEntity = offsetRepository
                .findByUserId(userId)
                .orElseGet(() -> {
                    RecommendationOffset ro = new RecommendationOffset();
                    ro.setUserId(userId);
                    return offsetRepository.save(ro);
                });

        int offset = offsetEntity.getOffsetValue();
        int cycle  = offsetEntity.getCycle();


        if (!Boolean.TRUE.equals(redis.hasKey(key))) {
            buildRecommendations(key, userId);
            offset = 0;
        }

        long total = redis.opsForList().size(key);


        if (offset >= total) {
            cycle++;
            offset = 0;
            buildRecommendations(key, userId);
            total = redis.opsForList().size(key);
        }


        List<String> rawIds = redis.opsForList()
                .range(key, offset, offset + limit - 1);

        List<Long> ids = rawIds.stream()
                .map(Long::parseLong)
                .toList();

        offsetRepository.updateOffsetAndCycle(userId, offset + ids.size(), cycle);

        List<User> users = userRepository.findAllByIdWithInterests(ids);

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        List<UserCardDto> ordered = ids.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .map(UserCardDto::from)
                .toList();

        return RecommendationResponse.builder()
                .users(ordered)
                .hasMore(true)
                .cycle(cycle)
                .build();
    }

    private void buildRecommendations(String key, Long userId) {
        List<Long> sortedIds = recommendationRepository.computeSortedFeedIds(userId);
        redis.delete(key);
        if (!sortedIds.isEmpty()) {
            redis.opsForList().rightPushAll(
                    key,
                    sortedIds.stream().map(String::valueOf).toList()
            );
        }
        redis.expire(key, TTL_HOURS, TimeUnit.HOURS);
    }


    @Transactional(readOnly = true)
    public FilteredRecommendationResponse getRecommendationsWithFilters(
            Long userId, RecommendationFiltersDto filters, int page, int limit
    ) {
        int offset = page * limit;

        List<Long> ids = recommendationRepository.findWithFilters(userId, filters, limit, offset);
        List<User> users = userRepository.findAllByIdWithInterests(ids);

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        List<UserCardDto> ordered = ids.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .map(UserCardDto::from)
                .toList();

        return FilteredRecommendationResponse.builder()
                .users(ordered)
                .hasMore(ids.size() == limit)
                .empty(ids.isEmpty())
                .build();
    }


    @Transactional
    public SwipeResponse swipe(Long fromUserId, SwipeRequest request) {
        Long toUserId = request.getToUserId();

        UserSwipe swipe = new UserSwipe();
        swipe.setFromUserId(fromUserId);
        swipe.setToUserId(toUserId);
        swipe.setReaction(request.getReaction());
        swipeRepository.save(swipe);

        if (request.getReaction() == Reaction.DISLIKE) {
            return SwipeResponse.builder()
                    .result("DISLIKED")
                    .build();
        }

        boolean mutualLike = swipeRepository.existsByFromUserIdAndToUserIdAndReaction(
                toUserId, fromUserId, Reaction.LIKE
        );

        if (!mutualLike) {
            return SwipeResponse.builder()
                    .result("LIKED")
                    .build();
        }

        Match match = new Match();
        match.setUserId1(fromUserId);
        match.setUserId2(toUserId);
        matchRepository.save(match);

        ChatSummaryResponseDto chatDto = chatService.createOrGetDirectChat(fromUserId, toUserId);

        redis.delete(RECOMMENDATION_KEY + fromUserId);
        redis.delete(RECOMMENDATION_KEY + toUserId);

        return SwipeResponse.builder()
                .result("MATCHED")
                .matchId(match.getId())
                .chatId(chatDto.id())
                .build();
    }
}