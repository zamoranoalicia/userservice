package org.azamorano.userService.application.user;

import io.jsonwebtoken.Claims;
import org.azamorano.userService.application.auth.SessionService;
import org.azamorano.userService.infrastructure.persistence.entity.PhoneEntity;
import org.azamorano.userService.infrastructure.persistence.entity.UserCredentialsEntity;
import org.azamorano.userService.infrastructure.persistence.entity.UserEntity;
import org.azamorano.userService.infrastructure.persistence.repository.UserCredentialsRepository;
import org.azamorano.userService.infrastructure.persistence.repository.UserRepository;
import org.azamorano.userService.infrastructure.security.JwtService;
import org.azamorano.userService.presentation.dto.request.PhoneRequest;
import org.azamorano.userService.presentation.dto.request.UserCreateRequest;
import org.azamorano.userService.presentation.dto.request.UserUpdateRequest;
import org.azamorano.userService.presentation.dto.response.PhoneResponse;
import org.azamorano.userService.presentation.dto.response.UserResponse;
import org.azamorano.userService.presentation.exception.AuthException;
import org.azamorano.userService.presentation.exception.UserCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {


    private final UserRepository userRepository;
    private final UserCredentialsRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final String duplicateEmailErrorMessage;
    private final JwtService jwtService;
    private final SessionService sessionService;

    public UserService(UserRepository userRepository, UserCredentialsRepository credentialRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${config.user.email.duplicateErrorMessage}") String duplicateEmailErrorMessage,
                       JwtService jwtService,
                       SessionService sessionService) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.duplicateEmailErrorMessage = duplicateEmailErrorMessage;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        String email = request.getEmail().toLowerCase();
        verifyExistingUser(email);

        OffsetDateTime now = OffsetDateTime.now();
        UserEntity user = buildUserFromRequest(request, now);

        if (request.getPhones() != null) {
            List<PhoneEntity> phones = buildPhones(request.getPhones(), user);
            user.getPhones().addAll(phones);
        }

        UserCredentialsEntity cred = buildCredentials(user, request.getPassword(), now);
        user.setCredentials(cred);

        UserEntity savedUser = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(savedUser.getId(), savedUser.getEmail());
        String refreshToken = jwtService.generateRefreshToken(savedUser.getId());
        try {
            Claims claims = jwtService.parseRefreshToken(refreshToken);
            sessionService.createSession(savedUser, refreshToken, claims);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user session", e);
        }

        List<PhoneResponse> phoneResponses = savedUser.getPhones().stream().map(ph ->
                new PhoneResponse(ph.getNumber(), ph.getCityCode(), ph.getCountryCode())
        ).collect(Collectors.toList());

        return new UserResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail(),
                savedUser.getCreatedAt(), savedUser.getModifiedAt(), savedUser.getLastLoginAt(),
                accessToken, savedUser.getIsActive(), phoneResponses);
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found", HttpStatus.NOT_FOUND));

        if (request == null) {
            throw new AuthException("Request body is required", HttpStatus.BAD_REQUEST);
        }

        boolean hasChanges = false;

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
            hasChanges = true;
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String normalizedEmail = request.getEmail().trim().toLowerCase();
            if (!normalizedEmail.equals(user.getEmail())) {
                userRepository.findByEmail(normalizedEmail)
                        .ifPresent(existingUser -> {
                            if (!existingUser.getId().equals(userId)) {
                                throw new UserCreationException(duplicateEmailErrorMessage, HttpStatus.CONFLICT);
                            }
                        });
                user.setEmail(normalizedEmail);
                hasChanges = true;
            }
        }

        if (request.getPhones() != null) {
            List<PhoneEntity> phones = buildPhones(request.getPhones(), user);
            user.getPhones().clear();
            user.getPhones().addAll(phones);
            hasChanges = true;
        }

        if (!hasChanges) {
            throw new AuthException("At least one field must be provided for update", HttpStatus.BAD_REQUEST);
        }

        user.setModifiedAt(OffsetDateTime.now());
        UserEntity savedUser = userRepository.save(user);

        return toUserResponse(savedUser);
    }


    private UserEntity buildUserFromRequest(UserCreateRequest request, OffsetDateTime now) {
        return UserEntity.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .email(request.getEmail().toLowerCase())
                .createdAt(now)
                .modifiedAt(now)
                .lastLoginAt(now)
                .isActive(true)
                .phones(new java.util.ArrayList<>())
                .sessions(new java.util.ArrayList<>())
                .build();
    }

    private void verifyExistingUser(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserCreationException(duplicateEmailErrorMessage, HttpStatus.CONFLICT);
        }
    }

    private List<PhoneEntity> buildPhones(List<PhoneRequest> phonesRequests, UserEntity user) {
        return phonesRequests.stream().map(p ->
                PhoneEntity.builder()
                        .phoneId(UUID.randomUUID())
                        .user(user)
                        .number(p.getNumber() == null ? null : String.valueOf(p.getNumber()))
                        .cityCode(p.getCityCode() == null ? null : String.valueOf(p.getCityCode()))
                        .countryCode(p.getCountryCode() == null ? null : String.valueOf(p.getCountryCode()))
                        .build()
        ).collect(Collectors.toList());
    }

    private UserCredentialsEntity buildCredentials(UserEntity user, String password, OffsetDateTime now) {
        return UserCredentialsEntity.builder()
                .user(user)
                .passwordHash(passwordEncoder.encode(password))
                .credentialUpdatedAt(now)
                .build();
    }

    private UserResponse toUserResponse(UserEntity user) {
        List<PhoneResponse> phoneResponses = user.getPhones().stream()
                .map(phone -> new PhoneResponse(phone.getNumber(), phone.getCityCode(), phone.getCountryCode()))
                .collect(Collectors.toList());

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phones(phoneResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                ));

        return toUserResponse(user);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .filter(UserEntity::getIsActive)
                .orElseThrow(() -> new AuthException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                ));

        userRepository.delete(user);
    }
}
