package com.luxus.tinterest.repository;


import com.luxus.tinterest.dto.recommendation.RecommendationFiltersDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RecommendationRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Long> computeSortedFeedIds(Long userId) {
        String sql = """
            SELECT u.id
            FROM users u
            WHERE u.id != ?
              AND u.blocked = false
              AND u.has_filled_profile = true
              AND u.id NOT IN (
                  SELECT to_user_id FROM user_swipes
                  WHERE from_user_id = ? AND reaction = 'LIKE'
              )
              AND u.id NOT IN (
                  SELECT to_user_id FROM user_swipes
                  WHERE from_user_id = ?
                    AND reaction = 'DISLIKE'
                    AND swiped_at > NOW() - INTERVAL '30 days'
              )
              AND u.id NOT IN (
                  SELECT CASE
                    WHEN user_id_1 = ? THEN user_id_2
                    ELSE user_id_1
                  END
                  FROM matches
                  WHERE user_id_1 = ? OR user_id_2 = ?
              )
            ORDER BY (
                (SELECT COUNT(*) FROM user_interests ui2
                 WHERE ui2.user_id = u.id
                   AND ui2.interest_id IN (
                       SELECT interest_id FROM user_interests WHERE user_id = ?
                   )
                ) * 10
                + CASE WHEN u.goal = (SELECT goal FROM users WHERE id = ?) THEN 20 ELSE 0 END
                + CASE WHEN u.city = (SELECT city FROM users WHERE id = ?) THEN 15 ELSE 0 END
                + CASE WHEN u.department = (SELECT department FROM users WHERE id = ?) THEN 5 ELSE 0 END
                + CASE
                    WHEN (SELECT personality_type FROM users WHERE id = ?) = 'EXTROVERT'
                         AND u.personality_type = 'INTROVERT' THEN 10
                    WHEN (SELECT personality_type FROM users WHERE id = ?) = 'INTROVERT'
                         AND u.personality_type = 'EXTROVERT' THEN 10
                    WHEN u.personality_type = 'AMBIVERT' THEN 5
                    ELSE 0
                  END
            ) DESC, u.id ASC
            """;

        return jdbcTemplate.queryForList(sql, Long.class,
                userId,  // u.id !=
                userId,  // LIKE подзапрос
                userId,  // DISLIKE подзапрос
                userId,  // matches CASE WHEN
                userId,  // matches WHERE user_id_1
                userId,  // matches WHERE user_id_2
                userId,  // COUNT интересов
                userId,  // goal
                userId,  // city
                userId,  // department
                userId,  // personality_type EXTROVERT
                userId   // personality_type INTROVERT
        );
    }

    public List<Long> findWithFilters(Long userId, RecommendationFiltersDto filters, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT u.id FROM users u ");

        List<Object> params = new ArrayList<>();

        boolean hasInterests = filters.getInterestIds() != null
                && !filters.getInterestIds().isEmpty();
        if (hasInterests) {
            sql.append(" JOIN user_interests ui ON ui.user_id = u.id ");
        }

        sql.append("""
            WHERE u.id != ?
              AND u.blocked = false
              AND u.has_filled_profile = true
              AND u.id NOT IN (
                  SELECT to_user_id FROM user_swipes
                  WHERE from_user_id = ? AND reaction = 'LIKE'
              )
              AND u.id NOT IN (
                  SELECT to_user_id FROM user_swipes
                  WHERE from_user_id = ?
                    AND reaction = 'DISLIKE'
                    AND swiped_at > NOW() - INTERVAL '30 days'
              )
              AND u.id NOT IN (
                  SELECT CASE WHEN user_id_1 = ? THEN user_id_2 ELSE user_id_1 END
                  FROM matches WHERE user_id_1 = ? OR user_id_2 = ?
              )
            """);

        params.add(userId);
        params.add(userId);
        params.add(userId);
        params.add(userId);
        params.add(userId);
        params.add(userId);

        if (filters.getCity() != null) {
            sql.append(" AND u.city = ? ");
            params.add(filters.getCity());
        }
        if (filters.getGoal() != null) {
            sql.append(" AND u.goal = ? ");
            params.add(filters.getGoal());
        }
        if (filters.getDepartment() != null) {
            sql.append(" AND u.department = ? ");
            params.add(filters.getDepartment());
        }
        if (filters.getGender() != null) {
            sql.append(" AND u.gender = ? ");
            params.add(filters.getGender());
        }
        if (filters.getPersonalityType() != null) {
            sql.append(" AND u.personality_type = ? ");
            params.add(filters.getPersonalityType());
        }
        if (hasInterests) {
            sql.append(" AND ui.interest_id IN (");
            sql.append(filters.getInterestIds().stream()
                    .map(id -> "?")
                    .collect(Collectors.joining(",")));
            sql.append(") ");
            params.addAll(filters.getInterestIds());
        }

        sql.append(" ORDER BY u.id ASC LIMIT ? OFFSET ? ");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.queryForList(sql.toString(), Long.class, params.toArray());
    }
}
