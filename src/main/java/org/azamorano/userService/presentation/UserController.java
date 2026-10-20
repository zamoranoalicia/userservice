package org.azamorano.userService.presentation;

import org.azamorano.userService.application.user.UserService;
import org.azamorano.userService.presentation.dto.request.UserCreateRequest;
import org.azamorano.userService.presentation.dto.request.UserUpdateRequest;
import org.azamorano.userService.presentation.dto.response.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.ok(created);
    }

    @PutMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserResponse updated = userService.updateUser(userId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteCurrentUser(
            @AuthenticationPrincipal UUID userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UUID userId
    ) {
        return ResponseEntity.ok(userService.getCurrentUser(userId));
    }
}
