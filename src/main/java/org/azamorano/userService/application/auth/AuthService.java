package org.azamorano.userService.application.auth;

import io.jsonwebtoken.Claims;
import org.azamorano.userService.infrastructure.persistence.entity.UserEntity;
import org.azamorano.userService.infrastructure.persistence.repository.UserRepository;
import org.azamorano.userService.presentation.dto.request.LoginRequest;
import org.azamorano.userService.presentation.dto.response.AuthResponse;
import org.azamorano.userService.presentation.exception.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final Authenticator authenticator;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final SessionService sessionService;

    public AuthService(Authenticator authenticator,
                       TokenProvider tokenProvider,
                       UserRepository userRepository,
                       SessionService sessionService) {
        this.authenticator = authenticator;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    @Transactional
    public AuthResponse authenticate(LoginRequest req) {
        UserDetails userDetails;
        try {
            userDetails = authenticator.authenticate(req.getUsername().toLowerCase(), req.getPassword());
        } catch (AuthException authException) {
            throw authException;
        } catch (Exception e) {
            throw new AuthException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        UUID userId = ((org.azamorano.userService.infrastructure.security.CustomUserDetails) userDetails).getUserId();

        AuthTokens tokens = tokenProvider.generateTokens(userId, userDetails.getUsername());

        Claims claims;
        try {
            claims = tokenProvider.parseClaims(tokens.getRefreshToken());
        } catch (Exception e) {
            throw new AuthException("Failed to create refresh token", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        UserEntity user = userRepository.findById(userId).orElseThrow(
                () -> new AuthException("User not found", HttpStatus.INTERNAL_SERVER_ERROR));

        sessionService.createSession(user, tokens.getRefreshToken(), claims);

        return new AuthResponse(tokens.getAccessToken(), tokens.getRefreshToken(), "Bearer");
    }
}
