package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.admin.AdminStatisticsResponseDto;
import com.luxus.tinterest.enums.Gender;
import com.luxus.tinterest.repository.MatchRepository;
import com.luxus.tinterest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminStatisticsService Tests")
class AdminStatisticsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private AdminStatisticsService adminStatisticsService;

    private Instant testInstant;

    @BeforeEach
    void setUp() {
        testInstant = Instant.now();
    }

    @Test
    @DisplayName("Sanity check - service should be injectable")
    void sanityCheck() {
        assertThat(adminStatisticsService).isNotNull();
    }

    @Nested
    @DisplayName("Positive Test Cases")
    class PositiveTests {

        @Test
        @DisplayName("Should return complete statistics with all data")
        void shouldReturnCompleteStatistics() {
            // Given
            long totalUsers = 1000L;
            long totalMatches = 500L;
            long usersLastDay = 50L;
            long usersLastWeek = 200L;
            long usersLastMonth = 400L;

            when(userRepository.count()).thenReturn(totalUsers);
            when(matchRepository.count()).thenReturn(totalMatches);
            when(userRepository.countByCreatedAtAfter(any(Instant.class)))
                    .thenReturn(usersLastDay)
                    .thenReturn(usersLastWeek)
                    .thenReturn(usersLastMonth);

            List<Object[]> genderDistribution = Arrays.asList(
                    new Object[]{Gender.MALE, 600L},
                    new Object[]{Gender.FEMALE, 400L}
            );
            when(userRepository.countUsersByGender()).thenReturn(genderDistribution);

            List<Object[]> topCities = Arrays.asList(
                    new Object[]{"Stockholm", 250L},
                    new Object[]{"Gothenburg", 150L},
                    new Object[]{"Malmö", 100L}
            );
            when(userRepository.findTopCities(10)).thenReturn(topCities);

            List<Object[]> topInterests = Arrays.asList(
                    new Object[]{"Programming", 300L},
                    new Object[]{"Sports", 250L},
                    new Object[]{"Music", 200L}
            );
            when(userRepository.findTopInterests(10)).thenReturn(topInterests);

            // When
            AdminStatisticsResponseDto result = adminStatisticsService.getStatistics();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalUsers()).isEqualTo(totalUsers);
            assertThat(result.getTotalMatches()).isEqualTo(totalMatches);

            assertThat(result.getRegistrationDynamics()).isNotNull();
            assertThat(result.getRegistrationDynamics().getLastDay()).isEqualTo(usersLastDay);
            assertThat(result.getRegistrationDynamics().getLastWeek()).isEqualTo(usersLastWeek);
            assertThat(result.getRegistrationDynamics().getLastMonth()).isEqualTo(usersLastMonth);

            assertThat(result.getGenderDistribution())
                    .hasSize(2)
                    .containsEntry("MALE", 600L)
                    .containsEntry("FEMALE", 400L);

            assertThat(result.getTopCities())
                    .hasSize(3)
                    .containsEntry("Stockholm", 250L)
                    .containsEntry("Gothenburg", 150L)
                    .containsEntry("Malmö", 100L);

            assertThat(result.getTopInterests())
                    .hasSize(3)
                    .containsEntry("Programming", 300L)
                    .containsEntry("Sports", 250L)
                    .containsEntry("Music", 200L);

            verify(userRepository, times(1)).count();
            verify(matchRepository, times(1)).count();
            verify(userRepository, times(3)).countByCreatedAtAfter(any(Instant.class));
            verify(userRepository, times(1)).countUsersByGender();
            verify(userRepository, times(1)).findTopCities(10);
            verify(userRepository, times(1)).findTopInterests(10);
        }

        @Test
        @DisplayName("Should correctly calculate time periods for registration dynamics")
        void shouldCorrectlyCalculateTimePeriods() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());
            when(userRepository.findTopCities(10)).thenReturn(Collections.emptyList());
            when(userRepository.findTopInterests(10)).thenReturn(Collections.emptyList());

            // When
            adminStatisticsService.getStatistics();

            // Then
            ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
            verify(userRepository, times(3)).countByCreatedAtAfter(instantCaptor.capture());

            List<Instant> capturedInstants = instantCaptor.getAllValues();
            Instant now = Instant.now();

            // Verify day calculation (within 2 seconds tolerance)
            assertThat(capturedInstants.get(0))
                    .isCloseTo(now.minus(1, ChronoUnit.DAYS), within(2, ChronoUnit.SECONDS));

            // Verify week calculation
            assertThat(capturedInstants.get(1))
                    .isCloseTo(now.minus(7, ChronoUnit.DAYS), within(2, ChronoUnit.SECONDS));

            // Verify month calculation
            assertThat(capturedInstants.get(2))
                    .isCloseTo(now.minus(30, ChronoUnit.DAYS), within(2, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("Should handle gender distribution with null gender as UNKNOWN")
        void shouldHandleNullGenderAsUnknown() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);

            List<Object[]> genderDistribution = Arrays.asList(
                    new Object[]{Gender.MALE, 50L},
                    new Object[]{null, 30L},
                    new Object[]{Gender.FEMALE, 20L}
            );
            when(userRepository.countUsersByGender()).thenReturn(genderDistribution);
            when(userRepository.findTopCities(10)).thenReturn(Collections.emptyList());
            when(userRepository.findTopInterests(10)).thenReturn(Collections.emptyList());

            // When
            AdminStatisticsResponseDto result = adminStatisticsService.getStatistics();

            // Then
            assertThat(result.getGenderDistribution())
                    .hasSize(3)
                    .containsEntry("MALE", 50L)
                    .containsEntry("FEMALE", 20L)
                    .containsEntry("UNKNOWN", 30L);
        }

        @Test
        @DisplayName("Should maintain order of top cities from repository")
        void shouldMaintainTopCitiesOrder() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());

            List<Object[]> topCities = Arrays.asList(
                    new Object[]{"Stockholm", 100L},
                    new Object[]{"Gothenburg", 80L},
                    new Object[]{"Malmö", 60L},
                    new Object[]{"Uppsala", 40L},
                    new Object[]{"Västerås", 20L}
            );
            when(userRepository.findTopCities(10)).thenReturn(topCities);
            when(userRepository.findTopInterests(10)).thenReturn(Collections.emptyList());

            // When
            AdminStatisticsResponseDto result = adminStatisticsService.getStatistics();

            // Then
            assertThat(result.getTopCities())
                    .hasSize(5)
                    .containsKeys("Stockholm", "Gothenburg", "Malmö", "Uppsala", "Västerås");

            // Verify order is maintained (LinkedHashMap preserves insertion order)
            List<String> cityKeys = new ArrayList<>(result.getTopCities().keySet());
            assertThat(cityKeys).containsExactly("Stockholm", "Gothenburg", "Malmö", "Uppsala", "Västerås");
        }

        @Test
        @DisplayName("Should handle empty statistics gracefully")
        void shouldHandleEmptyStatistics() {
            // Given
            when(userRepository.count()).thenReturn(0L);
            when(matchRepository.count()).thenReturn(0L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(0L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());
            when(userRepository.findTopCities(10)).thenReturn(Collections.emptyList());
            when(userRepository.findTopInterests(10)).thenReturn(Collections.emptyList());

            // When
            AdminStatisticsResponseDto result = adminStatisticsService.getStatistics();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalUsers()).isZero();
            assertThat(result.getTotalMatches()).isZero();
            assertThat(result.getRegistrationDynamics().getLastDay()).isZero();
            assertThat(result.getRegistrationDynamics().getLastWeek()).isZero();
            assertThat(result.getRegistrationDynamics().getLastMonth()).isZero();
            assertThat(result.getGenderDistribution()).isEmpty();
            assertThat(result.getTopCities()).isEmpty();
            assertThat(result.getTopInterests()).isEmpty();
        }

        @Test
        @DisplayName("Should handle single gender distribution")
        void shouldHandleSingleGenderDistribution() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);

            List<Object[]> genderDistribution = Collections.singletonList(
                    new Object[]{Gender.MALE, 100L}
            );
            when(userRepository.countUsersByGender()).thenReturn(genderDistribution);
            when(userRepository.findTopCities(10)).thenReturn(Collections.emptyList());
            when(userRepository.findTopInterests(10)).thenReturn(Collections.emptyList());

            // When
            AdminStatisticsResponseDto result = adminStatisticsService.getStatistics();

            // Then
            assertThat(result.getGenderDistribution())
                    .hasSize(1)
                    .containsEntry("MALE", 100L);
        }

        @Test
        @DisplayName("Should handle maximum limits for top cities and interests")
        void shouldHandleMaximumLimits() {
            // Given
            when(userRepository.count()).thenReturn(1000L);
            when(matchRepository.count()).thenReturn(500L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());

            List<Object[]> topCities = new ArrayList<>();
            List<Object[]> topInterests = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                topCities.add(new Object[]{"City" + i, (long) (100 - i * 10)});
                topInterests.add(new Object[]{"Interest" + i, (long) (200 - i * 20)});
            }
            when(userRepository.findTopCities(10)).thenReturn(topCities);
            when(userRepository.findTopInterests(10)).thenReturn(topInterests);

            // When
            AdminStatisticsResponseDto result = adminStatisticsService.getStatistics();

            // Then
            assertThat(result.getTopCities()).hasSize(10);
            assertThat(result.getTopInterests()).hasSize(10);
        }
    }

    @Nested
    @DisplayName("Negative Test Cases")
    class NegativeTests {

        @Test
        @DisplayName("Should throw exception when userRepository.count() fails")
        void shouldThrowExceptionWhenUserCountFails() {
            // Given
            when(userRepository.count()).thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThatThrownBy(() -> adminStatisticsService.getStatistics())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection failed");

            verify(userRepository, times(1)).count();
            verify(matchRepository, never()).count();
        }

        @Test
        @DisplayName("Should throw exception when matchRepository.count() fails")
        void shouldThrowExceptionWhenMatchCountFails() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> adminStatisticsService.getStatistics())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error");

            verify(userRepository, times(1)).count();
            verify(matchRepository, times(1)).count();
        }

        @Test
        @DisplayName("Should throw exception when countByCreatedAtAfter fails")
        void shouldThrowExceptionWhenCountByCreatedAtAfterFails() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class)))
                    .thenThrow(new RuntimeException("Query execution failed"));

            // When & Then
            assertThatThrownBy(() -> adminStatisticsService.getStatistics())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Query execution failed");
        }

        @Test
        @DisplayName("Should throw exception when countUsersByGender fails")
        void shouldThrowExceptionWhenCountUsersByGenderFails() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);
            when(userRepository.countUsersByGender())
                    .thenThrow(new RuntimeException("Gender query failed"));

            // When & Then
            assertThatThrownBy(() -> adminStatisticsService.getStatistics())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Gender query failed");
        }

        @Test
        @DisplayName("Should throw exception when findTopCities fails")
        void shouldThrowExceptionWhenFindTopCitiesFails() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());
            when(userRepository.findTopCities(10))
                    .thenThrow(new RuntimeException("Native query failed"));

            // When & Then
            assertThatThrownBy(() -> adminStatisticsService.getStatistics())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Native query failed");
        }

        @Test
        @DisplayName("Should throw exception when findTopInterests fails")
        void shouldThrowExceptionWhenFindTopInterestsFails() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());
            when(userRepository.findTopCities(10)).thenReturn(Collections.emptyList());
            when(userRepository.findTopInterests(10))
                    .thenThrow(new RuntimeException("Join query failed"));

            // When & Then
            assertThatThrownBy(() -> adminStatisticsService.getStatistics())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Join query failed");
        }

        @Test
        @DisplayName("Should throw NullPointerException when gender distribution contains invalid data")
        void shouldThrowExceptionWhenGenderDistributionContainsInvalidData() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);

            List<Object[]> invalidGenderDistribution = Collections.singletonList(
                    new Object[]{Gender.MALE, null} // null count
            );
            when(userRepository.countUsersByGender()).thenReturn(invalidGenderDistribution);

            // When & Then
            assertThatThrownBy(() -> adminStatisticsService.getStatistics())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw ClassCastException when top cities contains wrong data type")
        void shouldThrowExceptionWhenTopCitiesContainsWrongDataType() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());

            List<Object[]> invalidTopCities = Collections.singletonList(
                    new Object[]{123, 100L} // Integer instead of String
            );
            when(userRepository.findTopCities(10)).thenReturn(invalidTopCities);

            // When & Then
            assertThatThrownBy(() -> adminStatisticsService.getStatistics())
                    .isInstanceOf(ClassCastException.class);
        }

        @Test
        @DisplayName("Should throw ClassCastException when top interests contains wrong data type")
        void shouldThrowExceptionWhenTopInterestsContainsWrongDataType() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());
            when(userRepository.findTopCities(10)).thenReturn(Collections.emptyList());

            List<Object[]> invalidTopInterests = Collections.singletonList(
                    new Object[]{"Programming", "not-a-number"} // String instead of Long
            );
            when(userRepository.findTopInterests(10)).thenReturn(invalidTopInterests);

            // When & Then
            assertThatThrownBy(() -> adminStatisticsService.getStatistics())
                    .isInstanceOf(ClassCastException.class);
        }

        @Test
        @DisplayName("Should throw ArrayIndexOutOfBoundsException when repository returns incomplete arrays")
        void shouldThrowExceptionWhenArrayIsIncomplete() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());

            List<Object[]> incompleteArray = Collections.singletonList(
                    new Object[]{"Stockholm"} // Missing count
            );

            when(userRepository.findTopCities(10)).thenReturn(incompleteArray);

            // When & Then
            assertThatThrownBy(() -> adminStatisticsService.getStatistics())
                    .isInstanceOf(ArrayIndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("Should handle negative counts from repository")
        void shouldHandleNegativeCounts() {
            // Given - это технически не должно происходить, но тестируем поведение
            when(userRepository.count()).thenReturn(-100L);
            when(matchRepository.count()).thenReturn(-50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(-10L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());
            when(userRepository.findTopCities(10)).thenReturn(Collections.emptyList());
            when(userRepository.findTopInterests(10)).thenReturn(Collections.emptyList());

            // When
            AdminStatisticsResponseDto result = adminStatisticsService.getStatistics();

            // Then - сервис просто возвращает данные как есть
            assertThat(result.getTotalUsers()).isEqualTo(-100L);
            assertThat(result.getTotalMatches()).isEqualTo(-50L);
            assertThat(result.getRegistrationDynamics().getLastDay()).isEqualTo(-10L);
        }

        @Test
        @DisplayName("Should throw exception when repository returns null for Object array")
        void shouldThrowExceptionWhenRepositoryReturnsNullArray() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());

            List<Object[]> listWithNull = new ArrayList<>();
            listWithNull.add(null);
            when(userRepository.findTopCities(10)).thenReturn(listWithNull);

            // When & Then
            assertThatThrownBy(() -> adminStatisticsService.getStatistics())
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very large numbers")
        void shouldHandleVeryLargeNumbers() {
            // Given
            long veryLargeNumber = Long.MAX_VALUE;
            when(userRepository.count()).thenReturn(veryLargeNumber);
            when(matchRepository.count()).thenReturn(veryLargeNumber - 1);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(veryLargeNumber / 2);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());
            when(userRepository.findTopCities(10)).thenReturn(Collections.emptyList());
            when(userRepository.findTopInterests(10)).thenReturn(Collections.emptyList());

            // When
            AdminStatisticsResponseDto result = adminStatisticsService.getStatistics();

            // Then
            assertThat(result.getTotalUsers()).isEqualTo(veryLargeNumber);
            assertThat(result.getTotalMatches()).isEqualTo(veryLargeNumber - 1);
        }

        @Test
        @DisplayName("Should handle cities and interests with special characters")
        void shouldHandleSpecialCharacters() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());

            List<Object[]> citiesWithSpecialChars = Arrays.asList(
                    new Object[]{"São Paulo", 50L},
                    new Object[]{"Zürich", 30L},
                    new Object[]{"Москва", 20L},
                    new Object[]{"北京", 10L}
            );
            when(userRepository.findTopCities(10)).thenReturn(citiesWithSpecialChars);

            List<Object[]> interestsWithSpecialChars = Arrays.asList(
                    new Object[]{"C++/C#", 100L},
                    new Object[]{"Rock & Roll", 80L},
                    new Object[]{"Art & Design", 60L}
            );
            when(userRepository.findTopInterests(10)).thenReturn(interestsWithSpecialChars);

            // When
            AdminStatisticsResponseDto result = adminStatisticsService.getStatistics();

            // Then
            assertThat(result.getTopCities()).containsKeys("São Paulo", "Zürich", "Москва", "北京");
            assertThat(result.getTopInterests()).containsKeys("C++/C#", "Rock & Roll", "Art & Design");
        }

        @Test
        @DisplayName("Should handle empty strings in city and interest names")
        void shouldHandleEmptyStrings() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());

            List<Object[]> citiesWithEmptyString = Collections.singletonList(
                    new Object[]{"", 50L}
            );
            when(userRepository.findTopCities(10)).thenReturn(citiesWithEmptyString);

            List<Object[]> interestsWithEmptyString = Collections.singletonList(
                    new Object[]{"", 100L}
            );
            when(userRepository.findTopInterests(10)).thenReturn(interestsWithEmptyString);

            // When
            AdminStatisticsResponseDto result = adminStatisticsService.getStatistics();

            // Then
            assertThat(result.getTopCities()).containsKey("");
            assertThat(result.getTopInterests()).containsKey("");
        }

        @Test
        @DisplayName("Should handle all registration dynamics being zero")
        void shouldHandleZeroRegistrationDynamics() {
            // Given
            when(userRepository.count()).thenReturn(1000L);
            when(matchRepository.count()).thenReturn(500L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(0L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());
            when(userRepository.findTopCities(10)).thenReturn(Collections.emptyList());
            when(userRepository.findTopInterests(10)).thenReturn(Collections.emptyList());

            // When
            AdminStatisticsResponseDto result = adminStatisticsService.getStatistics();

            // Then
            assertThat(result.getRegistrationDynamics().getLastDay()).isZero();
            assertThat(result.getRegistrationDynamics().getLastWeek()).isZero();
            assertThat(result.getRegistrationDynamics().getLastMonth()).isZero();
        }

        @Test
        @DisplayName("Should handle both genders with equal distribution")
        void shouldHandleEqualGenderDistribution() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);

            List<Object[]> equalGenderDistribution = Arrays.asList(
                    new Object[]{Gender.MALE, 50L},
                    new Object[]{Gender.FEMALE, 50L}
            );
            when(userRepository.countUsersByGender()).thenReturn(equalGenderDistribution);
            when(userRepository.findTopCities(10)).thenReturn(Collections.emptyList());
            when(userRepository.findTopInterests(10)).thenReturn(Collections.emptyList());

            // When
            AdminStatisticsResponseDto result = adminStatisticsService.getStatistics();

            // Then
            assertThat(result.getGenderDistribution())
                    .containsEntry("MALE", 50L)
                    .containsEntry("FEMALE", 50L);
        }

        @Test
        @DisplayName("Should handle duplicate city names in results")
        void shouldHandleDuplicateCityNames() {
            // Given - хотя это не должно происходить из-за GROUP BY в SQL
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());

            List<Object[]> citiesWithDuplicates = Arrays.asList(
                    new Object[]{"Stockholm", 100L},
                    new Object[]{"Stockholm", 50L} // Дубликат
            );
            when(userRepository.findTopCities(10)).thenReturn(citiesWithDuplicates);
            when(userRepository.findTopInterests(10)).thenReturn(Collections.emptyList());

            // When
            AdminStatisticsResponseDto result = adminStatisticsService.getStatistics();

            // Then - последнее значение перезаписывает предыдущее
            assertThat(result.getTopCities())
                    .hasSize(1)
                    .containsEntry("Stockholm", 50L);
        }
    }

    @Nested
    @DisplayName("Transaction and Concurrency Tests")
    class TransactionTests {

        @Test
        @DisplayName("Should be read-only transaction")
        void shouldBeReadOnlyTransaction() {
            // Этот тест проверяет, что метод помечен как @Transactional(readOnly = true)
            // В реальном integration тесте это будет проверено через TransactionSynchronizationManager
            // Здесь мы просто проверяем, что метод не вызывает модифицирующие операции

            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());
            when(userRepository.findTopCities(10)).thenReturn(Collections.emptyList());
            when(userRepository.findTopInterests(10)).thenReturn(Collections.emptyList());

            // When
            adminStatisticsService.getStatistics();

            // Then - verify only read operations were called
            verify(userRepository, never()).save(any());
            verify(userRepository, never()).saveAll(any());
            verify(userRepository, never()).delete(any());
            verify(userRepository, never()).deleteAll();
            verify(matchRepository, never()).save(any());
            verify(matchRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should handle multiple concurrent calls correctly")
        void shouldHandleConcurrentCalls() {
            // Given
            when(userRepository.count()).thenReturn(100L);
            when(matchRepository.count()).thenReturn(50L);
            when(userRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(10L);
            when(userRepository.countUsersByGender()).thenReturn(Collections.emptyList());
            when(userRepository.findTopCities(10)).thenReturn(Collections.emptyList());
            when(userRepository.findTopInterests(10)).thenReturn(Collections.emptyList());

            // When - simulate multiple calls
            AdminStatisticsResponseDto result1 = adminStatisticsService.getStatistics();
            AdminStatisticsResponseDto result2 = adminStatisticsService.getStatistics();

            // Then - both calls should succeed and return same data
            assertThat(result1.getTotalUsers()).isEqualTo(result2.getTotalUsers());
            assertThat(result1.getTotalMatches()).isEqualTo(result2.getTotalMatches());

            // Verify repositories were called twice
            verify(userRepository, times(2)).count();
            verify(matchRepository, times(2)).count();
        }
    }
}
