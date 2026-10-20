package org.azamorano.userService.application.user;

import org.azamorano.userService.application.auth.SessionService;
import org.azamorano.userService.infrastructure.persistence.entity.UserEntity;
import org.azamorano.userService.infrastructure.persistence.repository.UserCredentialsRepository;
import org.azamorano.userService.infrastructure.persistence.repository.UserRepository;
import org.azamorano.userService.infrastructure.security.JwtService;
import org.azamorano.userService.presentation.dto.request.UserCreateRequest;
import org.azamorano.userService.presentation.dto.request.PhoneRequest;
import org.azamorano.userService.presentation.dto.request.UserUpdateRequest;
import org.azamorano.userService.presentation.dto.response.UserResponse;
import org.azamorano.userService.presentation.exception.AuthException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Test
    void whenCreateUserExpectedSuccess() {
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
        when(jwtService.parseRefreshToken("refresh-token")).thenReturn(io.jsonwebtoken.Jwts.claims());

        UserService svc = new UserService(userRepo, credRepo, passwordEncoder, "Duplicated email", jwtService, sessionService);

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
    void whenCreateUserDuplicateEmailThenThrowException() {
        UserRepository userRepo = mock(UserRepository.class);
        UserCredentialsRepository credRepo = mock(UserCredentialsRepository.class);
        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder = mock(org.springframework.security.crypto.password.PasswordEncoder.class);

        when(userRepo.findByEmail("jane@example.com")).thenReturn(Optional.of(new UserEntity()));

        JwtService jwtService = mock(JwtService.class);
        SessionService sessionService = mock(SessionService.class);
        when(jwtService.generateAccessToken(any(), anyString())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtService.parseRefreshToken("refresh-token")).thenReturn(io.jsonwebtoken.Jwts.claims());

        UserService svc = new UserService(userRepo, credRepo, passwordEncoder,
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

    @Test
    void whenGetCurrentUserSuccessExpectUserResponse() {
        UserRepository userRepo = mock(UserRepository.class);
        UserCredentialsRepository credRepo =
                mock(UserCredentialsRepository.class);
        var passwordEncoder =
                mock(org.springframework.security.crypto.password.PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        SessionService sessionService = mock(SessionService.class);

        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setCreatedAt(OffsetDateTime.now());
        user.setModifiedAt(OffsetDateTime.now());
        user.setLastLoginAt(OffsetDateTime.now());
        user.setIsActive(true);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        UserService svc = new UserService(
                userRepo, credRepo, passwordEncoder,
                "Duplicated email", jwtService, sessionService
        );

        UserResponse response = svc.getCurrentUser(userId);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("Jane Doe", response.getName());
        assertEquals("jane@example.com", response.getEmail());

        verify(userRepo).findById(userId);
        verify(userRepo, never()).save(any(UserEntity.class));
    }

    @Test
    void whenGetCurrentUserNotFoundExpectNotFound() {
        UserRepository userRepo = mock(UserRepository.class);
        UserCredentialsRepository credRepo =
                mock(UserCredentialsRepository.class);
        var passwordEncoder =
                mock(org.springframework.security.crypto.password.PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        SessionService sessionService = mock(SessionService.class);

        UUID userId = UUID.randomUUID();

        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        UserService svc = new UserService(
                userRepo, credRepo, passwordEncoder,
                "Duplicated email", jwtService, sessionService
        );

        AuthException exception = assertThrows(
                AuthException.class,
                () -> svc.getCurrentUser(userId)
        );

        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(userRepo).findById(userId);
        verify(userRepo, never()).save(any(UserEntity.class));
    }

    @Test
    void whenDeleteUserSuccessExpectNoContent() {
        UserRepository userRepo = mock(UserRepository.class);
        UserCredentialsRepository credRepo =
                mock(UserCredentialsRepository.class);
        var passwordEncoder =
                mock(org.springframework.security.crypto.password.PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        SessionService sessionService = mock(SessionService.class);

        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        UserService svc = new UserService(
                userRepo, credRepo, passwordEncoder,
                "Duplicated email", jwtService, sessionService
        );

        assertDoesNotThrow(() -> svc.deleteUser(userId));

        verify(userRepo).findById(userId);
        verify(userRepo).delete(user);
    }

    @Test
    void whenDeleteUserNotFoundExpectNotFound() {
        UserRepository userRepo = mock(UserRepository.class);
        UserCredentialsRepository credRepo =
                mock(UserCredentialsRepository.class);
        var passwordEncoder =
                mock(org.springframework.security.crypto.password.PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        SessionService sessionService = mock(SessionService.class);

        UUID userId = UUID.randomUUID();

        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        UserService svc = new UserService(
                userRepo, credRepo, passwordEncoder,
                "Duplicated email", jwtService, sessionService
        );

        AuthException exception = assertThrows(
                AuthException.class,
                () -> svc.deleteUser(userId)
        );

        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(userRepo).findById(userId);
        verify(userRepo, never()).delete(any(UserEntity.class));
    }

    @Test
    void whenUpdateUserSuccessExpectUpdatedUserResponse() {
        UserRepository userRepo = mock(UserRepository.class);
        UserCredentialsRepository credRepo =
                mock(UserCredentialsRepository.class);
        var passwordEncoder =
                mock(org.springframework.security.crypto.password.PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        SessionService sessionService = mock(SessionService.class);

        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setCreatedAt(OffsetDateTime.now());
        user.setModifiedAt(OffsetDateTime.now());
        user.setLastLoginAt(OffsetDateTime.now());
        user.setIsActive(true);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(userRepo.findByEmail("jane.updated@example.com"))
                .thenReturn(Optional.empty());
        when(userRepo.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserService svc = new UserService(
                userRepo, credRepo, passwordEncoder,
                "Duplicated email", jwtService, sessionService
        );


        UserUpdateRequest request = UserUpdateRequest.builder()
                .name("Jane Updated")
                .email("jane.updated@example.com")
                .build();

        UserResponse response = svc.updateUser(userId, request);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("Jane Updated", response.getName());
        assertEquals("jane.updated@example.com", response.getEmail());

        verify(userRepo).findById(userId);
        verify(userRepo).save(user);
    }
}
