package com.luxus.tinterest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxus.tinterest.dto.recommendation.FilteredRecommendationResponse;
import com.luxus.tinterest.dto.recommendation.InterestDto;
import com.luxus.tinterest.dto.recommendation.RecommendationFiltersDto;
import com.luxus.tinterest.dto.recommendation.RecommendationResponse;
import com.luxus.tinterest.dto.recommendation.SwipeRequest;
import com.luxus.tinterest.dto.recommendation.SwipeResponse;
import com.luxus.tinterest.dto.recommendation.UserCardDto;
import com.luxus.tinterest.enums.Gender;
import com.luxus.tinterest.enums.Reaction;
import com.luxus.tinterest.exception.GlobalExceptionHandler;
import com.luxus.tinterest.exception.common.UserNotFoundException;
import com.luxus.tinterest.service.RecommendationService;

import io.swagger.v3.core.util.Json;
import net.minidev.json.JSONObject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.json.JsonContent;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Recommendation Controller Tests")
class RecommendationControllerTests {

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private RecommendationController recommendationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(recommendationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(validator)
                .defaultRequest(get("/").with(authorized()))
                .addFilters(new AuthorizationHeaderFilter())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private void mockAuthenticationPrincipal(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private RequestPostProcessor authorized() {
        return request -> {
            request.addHeader("Authorization", "Bearer dummy-token");
            return request;
        };
    }

    private RequestPostProcessor unauthorized() {
        return request -> {
            request.removeHeader("Authorization");
            return request;
        };
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // GET /v1/discovery/recommendation - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully retrieve recommendation for user")
    void testGetRecommendation() throws Exception {
        mockAuthenticationPrincipal(1L);

        // Создаём объект InterestDto (предполагаемая структура)
        InterestDto interest = InterestDto.builder()
                .id(1L)
                .name("Reading")
                .build();

        // Создаём корректный UserCardDto через builder
        UserCardDto userCard = UserCardDto.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Doe")
                .middleName(null)
                .dateOfBirth(LocalDate.of(1998, 5, 15))
                .gender(Gender.FEMALE)
                .city("New York")
                .about("Loves hiking and coffee")
                .jobTitle("Engineer")
                .department("IT")
                .goal("Professional growth")
                .personalityType("INTJ")
                .timeSlots(List.of("10:00-12:00", "14:00-16:00"))
                .avatarUrl("jane.jpg")
                .interests(Set.of(interest))
                .build();

        // Создаём корректный RecommendationResponse
        RecommendationResponse response = RecommendationResponse.builder()
                .users(List.of(userCard))
                .hasMore(true)
                .cycle(1)
                .build();

        when(recommendationService.getRecommendations(anyLong(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(get("/v1/discovery/recommendation")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users[0].id").value(2))
                .andExpect(jsonPath("$.users[0].firstName").value("Jane"))
                .andExpect(jsonPath("$.users[0].lastName").value("Doe"))
                .andExpect(jsonPath("$.users[0].jobTitle").value("Engineer"))
                .andExpect(jsonPath("$.users[0].avatarUrl").value("jane.jpg"))
                .andExpect(jsonPath("$.users[0].gender").value("FEMALE"))
                .andExpect(jsonPath("$.users[0].interests[0].name").value("Reading"))
                .andExpect(jsonPath("$.hasMore").value(true))
                .andExpect(jsonPath("$.cycle").value(1));
    }

    @Test
    @DisplayName("Should retrieve recommendation with default limit")
    void testGetRecommendationDefaultLimit() throws Exception {
        mockAuthenticationPrincipal(1L);

        UserCardDto userCard = UserCardDto.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1998, 5, 15))
                .gender(Gender.FEMALE)
                .city("New York")
                .about("Loves hiking and coffee")
                .jobTitle("Engineer")
                .department("IT")
                .goal("Professional growth")
                .personalityType("INTJ")
                .timeSlots(List.of())
                .avatarUrl("jane.jpg")
                .interests(Set.of())
                .build();

        RecommendationResponse response = RecommendationResponse.builder()
                .users(List.of(userCard))
                .hasMore(true)
                .cycle(1)
                .build();

        when(recommendationService.getRecommendations(anyLong(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(get("/v1/discovery/recommendation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users[0].id").exists());
    }

    // -------------------------------------------------------------------------
    // GET /v1/discovery/recommendation - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 404 when user not found")
    void testGetRecommendationUserNotFound() throws Exception {
        mockAuthenticationPrincipal(999L);
        doThrow(new UserNotFoundException()).when(recommendationService).getRecommendations(anyLong(), anyInt());

        mockMvc.perform(get("/v1/discovery/recommendation"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when retrieving recommendation without authentication")
    void testGetRecommendationWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v1/discovery/recommendation").with(unauthorized()))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // GET /v1/discovery/recommendation/filter - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully retrieve filtered recommendations")
    void testGetFilteredRecommendations() throws Exception {
        mockAuthenticationPrincipal(1L);

        // Создаём UserCardDto для первого пользователя
        UserCardDto userCard1 = UserCardDto.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1998, 5, 15))
                .gender(Gender.FEMALE)
                .city("New York")
                .about("Loves hiking")
                .jobTitle("Engineer")
                .department("IT")
                .goal("Professional growth")
                .personalityType("INTJ")
                .timeSlots(List.of())
                .avatarUrl("jane.jpg")
                .interests(Set.of())
                .build();

        // Создаём UserCardDto для второго пользователя
        UserCardDto userCard2 = UserCardDto.builder()
                .id(3L)
                .firstName("Sarah")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1996, 8, 20))
                .gender(Gender.FEMALE)
                .city("Los Angeles")
                .about("Design enthusiast")
                .jobTitle("Designer")
                .department("UX")
                .goal("Creative excellence")
                .personalityType("ENFP")
                .timeSlots(List.of())
                .avatarUrl("sarah.jpg")
                .interests(Set.of())
                .build();

        FilteredRecommendationResponse response = FilteredRecommendationResponse.builder()
                .users(List.of(userCard1, userCard2))
                .hasMore(true)
                .empty(false)
                .build();

        when(recommendationService.getRecommendationsWithFilters(
                anyLong(),
                any(RecommendationFiltersDto.class),
                anyInt(),
                anyInt()))
                .thenReturn(response);

        mockMvc.perform(get("/v1/discovery/recommendation/filter")
                        .param("page", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users.length()").value(2))
                .andExpect(jsonPath("$.users[0].firstName").value("Jane"))
                .andExpect(jsonPath("$.users[0].id").value(2))
                .andExpect(jsonPath("$.users[1].firstName").value("Sarah"))
                .andExpect(jsonPath("$.users[1].id").value(3))
                .andExpect(jsonPath("$.hasMore").value(true))
                .andExpect(jsonPath("$.empty").value(false));
    }

    @Test
    @DisplayName("Should return empty result when no recommendations match filter")
    void testGetFilteredRecommendationsEmpty() throws Exception {
        mockAuthenticationPrincipal(1L);

        FilteredRecommendationResponse emptyResponse = FilteredRecommendationResponse.builder()
                .users(List.of())
                .hasMore(false)
                .empty(true)
                .build();

        when(recommendationService.getRecommendationsWithFilters(
                anyLong(),
                any(RecommendationFiltersDto.class),
                anyInt(),
                anyInt()))
                .thenReturn(emptyResponse);

        mockMvc.perform(get("/v1/discovery/recommendation/filter")
                        .param("page", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users.length()").value(0))
                .andExpect(jsonPath("$.hasMore").value(false))
                .andExpect(jsonPath("$.empty").value(true));
    }
    
    // -------------------------------------------------------------------------
    // GET /v1/discovery/recommendation/filter - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 400 for invalid pagination parameters")
    void testGetFilteredRecommendationsInvalidPage() throws Exception {
        
        mockMvc.perform(get("/v1/discovery/recommendation/filter")
                .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when user not found for filtered recommendations")
    void testGetFilteredRecommendationsUserNotFound() throws Exception {
        mockAuthenticationPrincipal(999L);
        doThrow(new UserNotFoundException()).when(recommendationService)
                .getRecommendationsWithFilters(anyLong(), any(RecommendationFiltersDto.class), anyInt(), anyInt());

        mockMvc.perform(get("/v1/discovery/recommendation/filter"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when retrieving filtered recommendations without authentication")
    void testGetFilteredRecommendationsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v1/discovery/recommendation/filter")
                .with(unauthorized())
                .param("page", "0")
                .param("limit", "10"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // POST /v1/discovery/swipe - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully record like swipe")
    void testSwipeLike() throws Exception {
        mockAuthenticationPrincipal(1L);

        // Создаём корректный SwipeRequest с enum Reaction
        SwipeRequest request = new SwipeRequest();
        request.setToUserId(2L);
        request.setReaction(Reaction.LIKE);  // enum, не строка "LIKE"

        // Создаём корректный SwipeResponse через builder
        SwipeResponse response = SwipeResponse.builder()
                .result("LIKED")
                .matchId(100L)
                .chatId(200L)
                .build();

        when(recommendationService.swipe(anyLong(), any(SwipeRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/v1/discovery/swipe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("LIKED"));
    }

    @Test
    @DisplayName("Should successfully record dislike swipe")
    void testSwipeDislike() throws Exception {
        mockAuthenticationPrincipal(1L);
        
        SwipeRequest request = new SwipeRequest();
        request.setToUserId(2L);
        request.setReaction(Reaction.DISLIKE);

        // Создаём корректный SwipeResponse через builder
        SwipeResponse response = SwipeResponse.builder()
                .result("DISLIKED")
                .matchId(100L) 
                .chatId(200L)   
                .build();

        when(recommendationService.swipe(anyLong(), any(SwipeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/discovery/swipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("DISLIKED"));
    }

    // -------------------------------------------------------------------------
    // POST /v1/discovery/swipe - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 400 when targetUserId is missing")
    void testSwipeWithoutTargetUserId() throws Exception {
        mockAuthenticationPrincipal(1L);
        SwipeRequest request = new SwipeRequest();
        request.setToUserId(null);
        request.setReaction(Reaction.LIKE);

        mockMvc.perform(post("/v1/discovery/swipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when reaction is missing")
    void testSwipeWithoutReactionType() throws Exception {
        mockAuthenticationPrincipal(1L);
        SwipeRequest request = new SwipeRequest();
        request.setToUserId(2L);
        request.setReaction(null);

        mockMvc.perform(post("/v1/discovery/swipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when reaction is invalid")
    void testSwipeInvalidReactionType() throws Exception {
        mockAuthenticationPrincipal(1L);
        SwipeRequest request = new SwipeRequest();
        request.setToUserId(2L);
        request.setReaction(null);

        String finalRequest = String.format("{\"toUserId\": \"%s\",\"reaction\": \"%s\" }", request.getToUserId(), "NOT_REACTION");

        mockMvc.perform(post("/v1/discovery/swipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(finalRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when target user not found")
    void testSwipeTargetUserNotFound() throws Exception {
        mockAuthenticationPrincipal(1L);
        SwipeRequest request = new SwipeRequest();
        request.setToUserId(999L);
        request.setReaction(Reaction.LIKE);
        doThrow(new UserNotFoundException()).when(recommendationService).swipe(anyLong(), any(SwipeRequest.class));

        mockMvc.perform(post("/v1/discovery/swipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when swiping without authentication")
    void testSwipeWithoutAuthentication() throws Exception {
        SwipeRequest request = new SwipeRequest();
        request.setToUserId(2L);
        request.setReaction(Reaction.LIKE);

        mockMvc.perform(post("/v1/discovery/swipe")
                .with(unauthorized())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    private static final class AuthorizationHeaderFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            if (httpRequest.getHeader("Authorization") == null) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            chain.doFilter(request, response);
        }
    }
}
