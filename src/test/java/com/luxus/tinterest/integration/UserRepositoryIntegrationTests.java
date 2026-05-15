package com.luxus.tinterest.integration;

import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.enums.Gender;
import com.luxus.tinterest.enums.Role;
import com.luxus.tinterest.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIntegrationTests extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should perform basic CRUD operations on User repository")
    void shouldPerformCrudOperations() {
        User user = User.builder()
                .firstName("Crud")
                .lastName("User")
                .email("crud@example.com")
                .emailVerified(true)
                .passwordHash("hash")
                .role(Role.USER)
                .city("Testville")
                .createdAt(Instant.now())
                .dateOfBirth(LocalDate.of(1992, 2, 2))
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.existsByEmail("crud@example.com")).isTrue();
        assertThat(userRepository.findByEmail("crud@example.com")).isPresent();

        saved.setCity("UpdatedCity");
        userRepository.save(saved);

        User updated = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getCity()).isEqualTo("UpdatedCity");

        userRepository.delete(updated);
        assertThat(userRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should execute custom query methods for User repository")
    void shouldExecuteCustomQueryMethods() {
        User first = User.builder()
                .firstName("First")
                .lastName("User")
                .email("first@example.com")
                .emailVerified(true)
                .passwordHash("hash")
                .role(Role.USER)
                .city("CityA")
                .createdAt(Instant.now())
                .build();

        User second = User.builder()
                .firstName("Second")
                .lastName("User")
                .email("second@example.com")
                .emailVerified(true)
                .passwordHash("hash")
                .role(Role.USER)
                .city("CityA")
                .createdAt(Instant.now())
                .build();

        userRepository.saveAll(List.of(first, second));

        long recentCount = userRepository.countByCreatedAtAfter(Instant.now().minusSeconds(60));
        assertThat(recentCount).isGreaterThanOrEqualTo(2);

        List<Object[]> topCities = userRepository.findTopCities(5);
        assertThat(topCities).isNotEmpty();
        assertThat(topCities.get(0)[0]).isEqualTo("CityA");
    }
}
