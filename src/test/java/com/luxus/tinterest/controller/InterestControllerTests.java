package com.luxus.tinterest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxus.tinterest.dto.interest.InterestResponseDto;
import com.luxus.tinterest.service.InterestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Interest Controller Tests")
class InterestControllerTests {

    @Mock
    private InterestService interestService;

    @InjectMocks
    private InterestController interestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(interestController)
                .defaultRequest(get("/").with(authorized()))
                .addFilters(new AuthorizationHeaderFilter())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should return 401 when retrieving interests without authorization")
    void testGetAllInterestsWithoutAuthorization() throws Exception {
        mockMvc.perform(get("/v1/interests").with(unauthorized()))
                .andExpect(status().isUnauthorized());
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

    // -------------------------------------------------------------------------
    // GET /v1/interests - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully retrieve all interests")
    void testGetAllInterests() throws Exception {
        InterestResponseDto interest1 = new InterestResponseDto(1L, "Travel");
        InterestResponseDto interest2 = new InterestResponseDto(2L, "Sports");
        InterestResponseDto interest3 = new InterestResponseDto(3L, "Music");

        when(interestService.getInterests()).thenReturn(List.of(interest1, interest2, interest3));

        mockMvc.perform(get("/v1/interests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Travel"))
                .andExpect(jsonPath("$[1].name").value("Sports"))
                .andExpect(jsonPath("$[2].name").value("Music"));
    }

    @Test
    @DisplayName("Should return empty list when no interests exist")
    void testGetAllInterestsEmpty() throws Exception {
        when(interestService.getInterests()).thenReturn(List.of());

        mockMvc.perform(get("/v1/interests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should verify interest response structure")
    void testInterestResponseStructure() throws Exception {
        InterestResponseDto interest = new InterestResponseDto(1L, "Photography");
        when(interestService.getInterests()).thenReturn(List.of(interest));

        mockMvc.perform(get("/v1/interests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Photography"));
    }
}
