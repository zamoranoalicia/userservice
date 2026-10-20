
package org.azamorano.userService.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String EMAIL_CLAIM = "email";
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final JwtService jwtService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            AuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtService = jwtService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader == null
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader
                .substring(BEARER_PREFIX.length())
                .trim();

        try {
            if (token.isBlank()) {
                throw new BadCredentialsException("Empty bearer token");
            }

            authenticateToken(token);

        } catch (JwtException
                 | IllegalArgumentException
                 | BadCredentialsException exception) {

            SecurityContextHolder.clearContext();

            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException("Invalid or expired token")
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateToken(String token) {
        Claims claims = jwtService.parseAccessToken(token);

        String subject = claims.getSubject();
        String email = claims.get(EMAIL_CLAIM, String.class);

        if (subject == null || subject.isBlank()
                || email == null || email.isBlank()) {
            throw new BadCredentialsException(
                    "Required token claims are missing"
            );
        }

        UUID userId = UUID.fromString(subject);

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority(DEFAULT_ROLE))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}
