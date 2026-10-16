package org.azamorano.userService.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "user_sessions")
@Data
public class UserSessionEntity {

    @Id
    @Column(name = "id")
    private UUID userSessionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_id", nullable = false)
    private String tokenId;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;
}
