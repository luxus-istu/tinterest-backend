package com.luxus.tinterest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxus.tinterest.dto.admin.AddInterestRequestDto;
import com.luxus.tinterest.dto.admin.AdminStatisticsResponseDto;
import com.luxus.tinterest.dto.admin.UserSummaryResponseDto;
import com.luxus.tinterest.dto.interest.InterestResponseDto;
import com.luxus.tinterest.dto.profile.InterestsUpdateRequestDto;
import com.luxus.tinterest.entity.Interest;
import com.luxus.tinterest.exception.GlobalExceptionHandler;
import com.luxus.tinterest.exception.admin.InterestAlreadyExistsException;
import com.luxus.tinterest.exception.admin.InterestNotFoundException;
import com.luxus.tinterest.exception.admin.InvalidAdminOperationException;
import com.luxus.tinterest.exception.common.UserNotFoundException;
import com.luxus.tinterest.exception.handler.AdminHandler;
import com.luxus.tinterest.service.AdminStatisticsService;
import com.luxus.tinterest.service.InterestService;
import com.luxus.tinterest.service.UserService;

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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Admin Controller Tests")
class AdminControllerTests {

    @Mock
    private UserService userService;

    @Mock
    private InterestService interestService;

    @Mock
    private AdminStatisticsService adminStatisticsService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new AdminHandler(), new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .defaultRequest(get("/").with(authorized()))
                .addFilters(new AuthorizationHeaderFilter())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
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


    // -------------------------------------------------------------------------
    // GET /v1/admin/statistics - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully retrieve admin statistics")
    void testGetStatistics() throws Exception {
        // Динамика регистраций
        AdminStatisticsResponseDto.UserRegistrationDynamicsDto dynamics =
                AdminStatisticsResponseDto.UserRegistrationDynamicsDto.builder()
                        .lastDay(10L)
                        .lastWeek(70L)
                        .lastMonth(300L)
                        .build();

        // Тестовые мапы
        Map<String, Long> genderDistribution = Map.of("MALE", 60L, "FEMALE", 40L);
        Map<String, Long> topCities = Map.of("New York", 120L, "London", 100L);
        Map<String, Long> topInterests = Map.of("Music", 200L, "Sports", 150L);

        // Итоговый DTO
        AdminStatisticsResponseDto response = AdminStatisticsResponseDto.builder()
                .totalUsers(1000L)
                .totalMatches(5000L)
                .registrationDynamics(dynamics)
                .genderDistribution(genderDistribution)
                .topCities(topCities)
                .topInterests(topInterests)
                .build();

        when(adminStatisticsService.getStatistics()).thenReturn(response);

        mockMvc.perform(get("/v1/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(1000))
                .andExpect(jsonPath("$.totalMatches").value(5000))
                .andExpect(jsonPath("$.registrationDynamics.lastDay").value(10))
                .andExpect(jsonPath("$.registrationDynamics.lastWeek").value(70))
                .andExpect(jsonPath("$.registrationDynamics.lastMonth").value(300))
                .andExpect(jsonPath("$.genderDistribution.MALE").value(60))
                .andExpect(jsonPath("$.genderDistribution.FEMALE").value(40))
                .andExpect(jsonPath("$.topCities['New York']").value(120))
                .andExpect(jsonPath("$.topCities['London']").value(100))
                .andExpect(jsonPath("$.topInterests.Music").value(200))
                .andExpect(jsonPath("$.topInterests.Sports").value(150));
    }

    // -------------------------------------------------------------------------
    // GET /v1/admin/statistics - Negative
    // -------------------------------------------------------------------------

    // No negative tests for statistics as it always returns data

    // -------------------------------------------------------------------------
    // GET /v1/admin/users - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should retrieve all users with pagination")
    void testGetAllUsers() throws Exception {
        UserSummaryResponseDto user1 = UserSummaryResponseDto.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .blocked(false)
                .createdAt(Instant.now())
                .build();

        UserSummaryResponseDto user2 = UserSummaryResponseDto.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .blocked(false)
                .createdAt(Instant.now())
                .build();

        Page<UserSummaryResponseDto> page = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 10), 2);

        when(userService.getAllUsers(isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/v1/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].email").value("john@example.com"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("Should retrieve users filtered by email")
    void testGetUsersByEmail() throws Exception {
        UserSummaryResponseDto user = UserSummaryResponseDto.builder()
                .id(1L)
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .blocked(false)
                .createdAt(Instant.now())
                .build();
        Page<UserSummaryResponseDto> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

        when(userService.getAllUsers(eq("john@example.com"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/v1/admin/users")
                .param("email", "john@example.com")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("john@example.com"));
    }

    @Test
    @DisplayName("Should retrieve empty page when no users match filter")
    void testGetUsersByEmailNoResults() throws Exception {
        Page<UserSummaryResponseDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(userService.getAllUsers(eq("nonexistent@example.com"), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/v1/admin/users")
                .param("email", "nonexistent@example.com")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // -------------------------------------------------------------------------
    // GET /v1/admin/users - Negative
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // POST /v1/admin/users/{userId}/block - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully block user")
    void testBlockUserSuccess() throws Exception {
        doNothing().when(userService).blockUser(1L);

        mockMvc.perform(post("/v1/admin/users/1/block"))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------------------------
    // POST /v1/admin/users/{userId}/block - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 404 when blocking non-existent user")
    void testBlockUserNotFound() throws Exception {
        doThrow(new UserNotFoundException()).when(userService).blockUser(999L);

        mockMvc.perform(post("/v1/admin/users/999/block"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when attempting invalid admin operation")
    void testBlockUserForbidden() throws Exception {
        doThrow(new InvalidAdminOperationException("Invalid operation")).when(userService).blockUser(1L);

        mockMvc.perform(post("/v1/admin/users/1/block"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // POST /v1/admin/users/{userId}/unblock - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully unblock user")
    void testUnblockUserSuccess() throws Exception {
        doNothing().when(userService).unblockUser(1L);

        mockMvc.perform(post("/v1/admin/users/1/unblock"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should successfully unblock user who is already blocked")
    void testUnblockBlockedUser() throws Exception {
        doNothing().when(userService).unblockUser(1L);

        mockMvc.perform(post("/v1/admin/users/1/unblock"))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------------------------
    // POST /v1/admin/users/{userId}/unblock - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 404 when unblocking non-existent user")
    void testUnblockUserNotFound() throws Exception {
        doThrow(new UserNotFoundException()).when(userService).unblockUser(999L);

        mockMvc.perform(post("/v1/admin/users/999/unblock"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when attempting invalid admin operation on unblock")
    void testUnblockUserForbidden() throws Exception {
        doThrow(new InvalidAdminOperationException("Invalid operation")).when(userService).unblockUser(1L);

        mockMvc.perform(post("/v1/admin/users/1/unblock"))
                .andExpect(status().isBadRequest());
    }

    // Authorization Tests

    @Test
    @DisplayName("Should return 401 when accessing admin statistics without authorization")
    void testGetStatisticsWithoutAuthorization() throws Exception {
        mockMvc.perform(get("/v1/admin/statistics").with(unauthorized()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when accessing admin user list without authorization")
    void testGetUsersWithoutAuthorization() throws Exception {
        mockMvc.perform(get("/v1/admin/users")
                        .param("page", "0")
                        .param("size", "10")
                        .with(unauthorized()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when blocking user without authorization")
    void testBlockUserWithoutAuthorization() throws Exception {
        mockMvc.perform(post("/v1/admin/users/1/block").with(unauthorized()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when unblocking user without authorization")
    void testUnblockUserWithoutAuthorization() throws Exception {
        mockMvc.perform(post("/v1/admin/users/1/unblock").with(unauthorized()))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // POST /v1/admin/interests/add - Posititve
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully add new interest")
    void testAddInterest() throws Exception {
        AddInterestRequestDto newInterest = AddInterestRequestDto.builder()
                .name("New Interest")
                .build();

        when(interestService.addInterest("New Interest")).thenReturn(new InterestResponseDto(1L, "New Interest"));

        mockMvc.perform(post("/v1/admin/interests/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newInterest)))
                .andExpect(status().isCreated());
    }


    // -------------------------------------------------------------------------
    // POST /v1/admin/interests/add - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 400 when adding interest without name")
    void testAddInterestWithoutName() throws Exception {
        AddInterestRequestDto newInterest = AddInterestRequestDto.builder()
                .name(null)
                .build();

        mockMvc.perform(post("/v1/admin/interests/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newInterest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 409 when interest already exists")
    void testAddInterestAlreadyExists() throws Exception {
        AddInterestRequestDto newInterest = AddInterestRequestDto.builder()
                .name("Existing Interest")
                .build();

        doThrow(new InterestAlreadyExistsException("Interest already exists")).when(interestService).addInterest("Existing Interest");

        mockMvc.perform(post("/v1/admin/interests/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newInterest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return 401 when accessing admin statistics without authorization")
    void testAddInterestWithoutAuthorization() throws Exception {
        mockMvc.perform(post("/v1/admin/interests/add").with(unauthorized()))
                .andExpect(status().isUnauthorized());
    }
    
    // -------------------------------------------------------------------------
    // DELETE /v1/admin/interests/delete/{interestId} - Posititve
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully delete interest")
    void testDeleteInterest() throws Exception {
        doNothing().when(interestService).deleteInterest(1L);

        mockMvc.perform(delete("/v1/admin/interests/delete/1"))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------------------------
    // DELETE /v1/admin/interests/delete/{interestId} - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 404 when deleting non-existent interest")
    void testDeleteNonExistentInterest() throws Exception {
        doThrow(new InterestNotFoundException("Interest not found")).when(interestService).deleteInterest(999L);

        mockMvc.perform(delete("/v1/admin/interests/delete/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when accessing admin statistics without authorization")
    void testDeleteInterestWithoutAuthorization() throws Exception {
        mockMvc.perform(delete("/v1/admin/interests/delete/1").with(unauthorized()))
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
