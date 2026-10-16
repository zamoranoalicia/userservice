package org.azamorano.userService.presentation.exception;

import org.springframework.http.HttpStatus;

public class UserCreationException extends RuntimeException {
    private final HttpStatus status;

    public UserCreationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
