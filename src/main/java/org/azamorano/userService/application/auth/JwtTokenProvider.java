package org.azamorano.userService.application.auth;

import io.jsonwebtoken.Claims;
import org.azamorano.userService.infrastructure.security.JwtService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JwtTokenProvider implements TokenProvider {

    private final JwtService jwtService;

    public JwtTokenProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public AuthTokens generateTokens(UUID userId, String email) {
        String access = jwtService.generateAccessToken(userId, email);
        String refresh = jwtService.generateRefreshToken(userId);
        return new AuthTokens(access, refresh);
    }

    @Override
    public Claims parseClaims(String token) {
        return jwtService.parseRefreshToken(token);
    }
}
