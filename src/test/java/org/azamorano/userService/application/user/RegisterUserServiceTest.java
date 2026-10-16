package org.azamorano.userService.application.user;

import org.azamorano.userService.infrastructure.persistence.entity.UserEntity;
import org.azamorano.userService.infrastructure.persistence.repository.UserCredentialsRepository;
import org.azamorano.userService.infrastructure.persistence.repository.UserRepository;
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

        RegisterUserService svc = new RegisterUserService(userRepo, credRepo, passwordEncoder, "Duplicated email");

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

        verify(userRepo).findByEmail("jane@example.com");
        verify(userRepo).save(any(UserEntity.class));
    }

    @Test
    void createUser_duplicateEmail() {
        UserRepository userRepo = mock(UserRepository.class);
        UserCredentialsRepository credRepo = mock(UserCredentialsRepository.class);
        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder = mock(org.springframework.security.crypto.password.PasswordEncoder.class);

        when(userRepo.findByEmail("jane@example.com")).thenReturn(Optional.of(new UserEntity()));

        RegisterUserService svc = new RegisterUserService(userRepo, credRepo, passwordEncoder,
                "El correo ya ha sido registrado");

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
