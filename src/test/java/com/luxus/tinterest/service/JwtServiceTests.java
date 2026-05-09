package com.luxus.tinterest.service;

import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTests {

    @Test
    @DisplayName("Should generate and validate access token")
    void testGenerateAndValidateAccessToken() throws Exception {
        JwtService jwtService = new JwtService(
                new ClassPathResource("test-keys/private.pem"),
                new ClassPathResource("test-keys/public.pem")
        );
        java.lang.reflect.Field expirationField = JwtService.class.getDeclaredField("expirationMs");
        expirationField.setAccessible(true);
        expirationField.setLong(jwtService, 900000L);

        User user = User.builder()
                .id(1L)
                .role(Role.USER)
                .build();

        String token = jwtService.generateAccessToken(user);
        assertNotNull(token);

        Claims claims = jwtService.validateAndExtract(token);
        assertEquals("1", claims.getSubject());
        assertEquals("USER", claims.get("role", String.class));
    }

    @Test
    @DisplayName("Should throw when token is invalid")
    void testValidateThrowsForInvalidToken() throws Exception {
        JwtService jwtService = new JwtService(
                new ClassPathResource("test-keys/private.pem"),
                new ClassPathResource("test-keys/public.pem")
        );
        java.lang.reflect.Field expirationField = JwtService.class.getDeclaredField("expirationMs");
        expirationField.setAccessible(true);
        expirationField.setLong(jwtService, 900000L);

        assertThrows(JwtException.class,
                () -> jwtService.validateAndExtract("invalid.token.value"));
    }
}
