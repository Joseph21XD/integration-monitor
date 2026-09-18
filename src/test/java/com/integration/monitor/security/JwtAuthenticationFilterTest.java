package com.integration.monitor.security;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.integration.monitor.config.JwtProperties;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {

        JwtProperties properties
                = new JwtProperties();

        properties.setSecret(
                "integration-monitor-test-secret-key-2026-very-long"
        );

        properties.setExpirationMinutes(60);

        jwtService
                = new JwtService(properties);

        filter
                = new JwtAuthenticationFilter(jwtService);

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateUserWithValidToken()
            throws ServletException, IOException {

        String token
                = jwtService.generateToken(
                        "admin",
                        List.of("ADMIN", "USER")
                );

        MockHttpServletRequest request
                = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response
                = new MockHttpServletResponse();

        FilterChain filterChain
                = mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        Authentication authentication
                = SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);
        assertEquals(
                "admin",
                authentication.getName()
        );

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(
                                authority
                                -> authority.getAuthority()
                                        .equals("ROLE_ADMIN")
                        )
        );

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(
                                authority
                                -> authority.getAuthority()
                                        .equals("ROLE_USER")
                        )
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldContinueWithoutAuthenticationWhenTokenIsMissing()
            throws ServletException, IOException {

        MockHttpServletRequest request
                = new MockHttpServletRequest();

        MockHttpServletResponse response
                = new MockHttpServletResponse();

        FilterChain filterChain
                = mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldContinueWithoutAuthenticationWhenTokenIsInvalid()
            throws ServletException, IOException {

        MockHttpServletRequest request
                = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer invalid-token"
        );

        MockHttpServletResponse response
                = new MockHttpServletResponse();

        FilterChain filterChain
                = mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWithTamperedToken() throws ServletException, IOException {
        String token = jwtService.generateToken("admin", List.of("ADMIN"));
        String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tamperedToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        filter.doFilter(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
