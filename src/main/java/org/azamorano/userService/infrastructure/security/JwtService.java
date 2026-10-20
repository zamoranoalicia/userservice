
package org.azamorano.userService.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final String EMAIL_CLAIM = "email";
    private static final String TOKEN_TYPE_CLAIM = "token_type";

    private static final String ACCESS_TOKEN = "access";
    private static final String REFRESH_TOKEN = "refresh";

    private final SecretKey key;
    private final long accessTokenMinutes;
    private final long refreshTokenDays;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.accessMinutes}") long accessTokenMinutes,
            @Value("${security.jwt.refreshDays}") long refreshTokenDays
    ) {
        this.key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        if (accessTokenMinutes <= 0 || refreshTokenDays <= 0) {
            throw new IllegalArgumentException(
                    "JWT expiration settings must be positive"
            );
        }

        this.accessTokenMinutes = accessTokenMinutes;
        this.refreshTokenDays = refreshTokenDays;
    }

    public String generateAccessToken(UUID userId, String email) {
        return generateToken(
                userId,
                email,
                ACCESS_TOKEN,
                accessTokenMinutes,
                ChronoUnit.MINUTES
        );
    }

    public String generateRefreshToken(UUID userId) {
        return generateToken(
                userId,
                null,
                REFRESH_TOKEN,
                refreshTokenDays,
                ChronoUnit.DAYS
        );
    }

    public Claims parseAccessToken(String token) {
        return parseTokenOfType(token, ACCESS_TOKEN);
    }

    public Claims parseRefreshToken(String token) {
        return parseTokenOfType(token, REFRESH_TOKEN);
    }

    private String generateToken(
            UUID userId,
            String email,
            String tokenType,
            long expiration,
            ChronoUnit unit
    ) {
        Instant now = Instant.now();

        var builder = Jwts.builder()
                .setSubject(userId.toString())
                .setId(UUID.randomUUID().toString())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .setIssuedAt(Date.from(now))
                .setExpiration(
                        Date.from(now.plus(expiration, unit))
                );

        if (email != null) {
            builder.claim(EMAIL_CLAIM, email);
        }

        return builder
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private Claims parseTokenOfType(
            String token,
            String expectedType
    ) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String actualType = claims.get(TOKEN_TYPE_CLAIM, String.class);

        if (!expectedType.equals(actualType)) {
            throw new JwtException("Unexpected JWT token type");
        }

        return claims;
    }
}
