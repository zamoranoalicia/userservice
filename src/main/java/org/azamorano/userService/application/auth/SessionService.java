package org.azamorano.userService.application.auth;

import io.jsonwebtoken.Claims;
import org.azamorano.userService.infrastructure.persistence.entity.UserEntity;
import org.azamorano.userService.infrastructure.persistence.entity.UserSessionEntity;
import org.azamorano.userService.infrastructure.persistence.repository.UserSessionRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class SessionService {

    private final UserSessionRepository sessionRepository;

    public SessionService(UserSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public UserSessionEntity createSession(UserEntity user, String refreshToken, Claims claims) {
        String jti = claims.getId();
        String tokenHash = sha256(refreshToken);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = OffsetDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneOffset.UTC);

        UserSessionEntity session = UserSessionEntity.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenId(jti)
                .tokenHash(tokenHash)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        return sessionRepository.save(session);
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
