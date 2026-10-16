package org.azamorano.userService.infrastructure.security;

import io.jsonwebtoken.Claims;
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

    private final SecretKey key;
    private final long accessTokenMinutes;
    private final long refreshTokenDays;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.accessMinutes}") long accessTokenMinutes,
                      @Value("${security.jwt.refreshDays}") long refreshTokenDays) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = accessTokenMinutes;
        this.refreshTokenDays = refreshTokenDays;
    }

    public String generateAccessToken(UUID userId, String email) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();
        Date exp = Date.from(now.plus(accessTokenMinutes, ChronoUnit.MINUTES));

        return Jwts.builder()
                .setSubject(userId.toString())
                .setId(jti)
                .claim("email", email)
                .setIssuedAt(Date.from(now))
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();
        Date exp = Date.from(now.plus(refreshTokenDays, ChronoUnit.DAYS));

        return Jwts.builder()
                .setSubject(userId.toString())
                .setId(jti)
                .setIssuedAt(Date.from(now))
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
