package org.azamorano.userService.presentation;

import org.azamorano.userService.presentation.dto.response.ErrorResponse;
import org.azamorano.userService.presentation.exception.UserCreationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.validation.ConstraintViolationException;

import java.util.stream.Collectors;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(UserCreationException.class)
    public ResponseEntity<ErrorResponse> handleUserCreation(UserCreationException ex) {
        ErrorResponse err = ErrorResponse.builder().message(ex.getMessage()).build();
        return ResponseEntity.status(ex.getStatus()).body(err);
    }

    @ExceptionHandler(org.azamorano.userService.presentation.exception.AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(org.azamorano.userService.presentation.exception.AuthException ex) {
        ErrorResponse err = ErrorResponse.builder().message(ex.getMessage()).build();
        return ResponseEntity.status(ex.getStatus()).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ErrorResponse err = ErrorResponse.builder().message(msg).build();
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ErrorResponse err = ErrorResponse.builder().message(msg).build();
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        ErrorResponse err = ErrorResponse.builder().message(msg).build();
        return ResponseEntity.badRequest().body(err);
    }
}
