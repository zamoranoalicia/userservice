package org.azamorano.userService.application.auth;

import io.jsonwebtoken.Claims;

import java.util.UUID;

public interface TokenProvider {
    AuthTokens generateTokens(UUID userId, String email);
    Claims parseClaims(String token);
}
