package org.azamorano.userService.application.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.azamorano.userService.infrastructure.persistence.entity.UserEntity;
import org.azamorano.userService.infrastructure.persistence.repository.UserRepository;
import org.azamorano.userService.infrastructure.security.CustomUserDetails;
import org.azamorano.userService.infrastructure.security.CustomUserDetailsService;
import org.azamorano.userService.infrastructure.security.JwtService;
import org.azamorano.userService.presentation.dto.request.LoginRequest;
import org.azamorano.userService.presentation.dto.response.AuthResponse;
import org.junit.jupiter.api.Test;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceSolidTest {

    @Test
    void authenticate_success() {
        Authenticator authenticator = mock(Authenticator.class);
        TokenProvider tokenProvider = mock(TokenProvider.class);
        org.azamorano.userService.infrastructure.persistence.repository.UserRepository userRepository = mock(org.azamorano.userService.infrastructure.persistence.repository.UserRepository.class);
        SessionService sessionService = mock(SessionService.class);

        UUID userId = UUID.randomUUID();
        CustomUserDetails userDetails = new CustomUserDetails(userId, "jane@example.com", "hashed", true);
        when(authenticator.authenticate("jane@example.com", "P@ssw0rd!123")).thenReturn(userDetails);

        when(tokenProvider.generateTokens(userId, "jane@example.com")).thenReturn(new AuthTokens("access-token", "refresh-token"));

        UserEntity user = new UserEntity();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        AuthService authService = new AuthService(authenticator, tokenProvider, userRepository, sessionService);

        LoginRequest loginRequest = LoginRequest.builder().username("jane@example.com").password("P@ssw0rd!123").build();

        Claims claims = Jwts.claims();
        claims.setSubject(userId.toString());
        claims.setId("refresh-token-id");
        claims.setExpiration(Date.from(OffsetDateTime.now().plusDays(7).toInstant()));

        when(tokenProvider.parseClaims("refresh-token"))
                .thenReturn(claims);
        AuthResponse authResponse = authService.authenticate(loginRequest);

        assertEquals("access-token", authResponse.getAccessToken());
        assertEquals("refresh-token", authResponse.getRefreshToken());

        verify(sessionService).createSession(eq(user), eq("refresh-token"), any(Claims.class));
    }

    @Test
    void authenticate_wrongPassword() {
        Authenticator authenticator = mock(Authenticator.class);
        TokenProvider tokenProvider = mock(TokenProvider.class);
        org.azamorano.userService.infrastructure.persistence.repository.UserRepository userRepository = mock(org.azamorano.userService.infrastructure.persistence.repository.UserRepository.class);
        SessionService sessionService = mock(SessionService.class);

        when(authenticator.authenticate("jane@example.com", "wrong")).thenThrow(new org.azamorano.userService.presentation.exception.AuthException("Invalid credentials", org.springframework.http.HttpStatus.UNAUTHORIZED));

        AuthService authService = new AuthService(authenticator, tokenProvider, userRepository, sessionService);

        LoginRequest loginRequest = LoginRequest.builder().username("jane@example.com").password("wrong").build();

        assertThrows(org.azamorano.userService.presentation.exception.AuthException.class, () -> authService.authenticate(loginRequest));
        verify(sessionService, never()).createSession(any(), anyString(), any());
    }
}
