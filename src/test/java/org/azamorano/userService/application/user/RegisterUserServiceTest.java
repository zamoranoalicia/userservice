package org.azamorano.userService.application.user;

import org.azamorano.userService.application.auth.SessionService;
import org.azamorano.userService.infrastructure.persistence.entity.UserEntity;
import org.azamorano.userService.infrastructure.persistence.repository.UserCredentialsRepository;
import org.azamorano.userService.infrastructure.persistence.repository.UserRepository;
import org.azamorano.userService.infrastructure.security.JwtService;
import org.azamorano.userService.presentation.dto.request.UserCreateRequest;
import org.azamorano.userService.presentation.dto.request.PhoneRequest;
import org.azamorano.userService.presentation.dto.response.UserResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegisterUserServiceTest {

    @Test
    void createUser_success() {
        UserRepository userRepo = mock(UserRepository.class);
        UserCredentialsRepository credRepo = mock(UserCredentialsRepository.class);
        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder = mock(org.springframework.security.crypto.password.PasswordEncoder.class);

        when(userRepo.findByEmail("jane@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepo.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JwtService jwtService = mock(JwtService.class);
        SessionService sessionService = mock(SessionService.class);
        when(jwtService.generateAccessToken(any(), anyString())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtService.parseClaims("refresh-token")).thenReturn(io.jsonwebtoken.Jwts.claims());

        RegisterUserService svc = new RegisterUserService(userRepo, credRepo, passwordEncoder, "Duplicated email", jwtService, sessionService);

        UserCreateRequest req = UserCreateRequest.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .password("P@ssw0rd!")
                .phones(List.of(new PhoneRequest(1234567890L, 1, 57)))
                .build();

        UserResponse resp = svc.createUser(req);

        assertNotNull(resp.getId());
        assertEquals("Jane Doe", resp.getName());
        assertEquals("jane@example.com", resp.getEmail());
        assertNotNull(resp.getCreatedAt());
        assertEquals(1, resp.getPhones().size());
        assertEquals("access-token", resp.getToken());

        verify(userRepo).findByEmail("jane@example.com");
        verify(userRepo).save(any(UserEntity.class));
        verify(sessionService).createSession(any(), eq("refresh-token"), any());
    }

    @Test
    void createUser_duplicateEmail() {
        UserRepository userRepo = mock(UserRepository.class);
        UserCredentialsRepository credRepo = mock(UserCredentialsRepository.class);
        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder = mock(org.springframework.security.crypto.password.PasswordEncoder.class);

        when(userRepo.findByEmail("jane@example.com")).thenReturn(Optional.of(new UserEntity()));

        JwtService jwtService = mock(JwtService.class);
        SessionService sessionService = mock(SessionService.class);
        when(jwtService.generateAccessToken(any(), anyString())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtService.parseClaims("refresh-token")).thenReturn(io.jsonwebtoken.Jwts.claims());

        RegisterUserService svc = new RegisterUserService(userRepo, credRepo, passwordEncoder,
                "El correo ya ha sido registrado", jwtService, sessionService);

        UserCreateRequest req = UserCreateRequest.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .password("P@ssw0rd!")
                .build();

        org.azamorano.userService.presentation.exception.UserCreationException ex = assertThrows(org.azamorano.userService.presentation.exception.UserCreationException.class, () -> svc.createUser(req));
        assertEquals("El correo ya ha sido registrado", ex.getMessage());

        verify(userRepo).findByEmail("jane@example.com");
        verify(userRepo, never()).save(any());
    }
}
