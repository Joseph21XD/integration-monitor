package com.integration.monitor.security;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.integration.monitor.config.JwtProperties;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {

        JwtProperties properties = new JwtProperties();

        properties.setSecret(
                "integration-monitor-test-secret-key-2026-very-long"
        );

        properties.setExpirationMinutes(60);

        jwtService = new JwtService(properties);
    }

    @Test
    void shouldGenerateValidToken() {

        List<String> roles
                = List.of("ADMIN", "USER");

        String token
                = jwtService.generateToken(
                        "admin",
                        roles
                );

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractUsernameFromToken() {

        List<String> roles
                = List.of("ADMIN");

        String token
                = jwtService.generateToken(
                        "admin",
                        roles
                );

        String username
                = jwtService.extractUsername(token);

        assertEquals("admin", username);
    }

    @Test
    void shouldExtractRolesFromToken() {

        List<String> roles
                = List.of("ADMIN", "USER");

        String token
                = jwtService.generateToken(
                        "admin",
                        roles
                );

        List<String> extractedRoles
                = jwtService.extractRoles(token);

        assertEquals(roles, extractedRoles);
    }

    @Test
    void shouldValidateValidToken() {

        String token
                = jwtService.generateToken(
                        "admin",
                        List.of("ADMIN")
                );

        assertTrue(
                jwtService.isTokenValid(token)
        );
    }

    @Test
    void shouldRejectTamperedToken() {

        String token
                = jwtService.generateToken(
                        "admin",
                        List.of("ADMIN")
                );

        String tamperedToken
                = token.substring(0, token.length() - 5)
                + "xxxxx";

        assertFalse(
                jwtService.isTokenValid(tamperedToken)
        );
    }

    @Test

    void shouldRejectEmptyToken() {

        assertFalse(
                jwtService.isTokenValid("")
        );
    }

    @Test

    void shouldRejectInvalidToken() {

        assertFalse(
                jwtService.isTokenValid("this-is-not-a-jwt")
        );
    }
}
