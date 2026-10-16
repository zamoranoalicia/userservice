package org.azamorano.userService.presentation.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {
    private static final String INVALID_PASSWORD = "Invalid Password: ";
    private final Pattern passwordPattern;
    private final String errorMessage;

    public PasswordValidator(@Value("${config.password.regex}") String passwordPattern,
                             @Value("${config.password.errorMessage}") String errorMessage) {
        this.passwordPattern =  Pattern.compile(passwordPattern);
        this.errorMessage = errorMessage;
    }
    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        if (!passwordPattern.matcher(password).matches()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    INVALID_PASSWORD.concat(errorMessage)).addConstraintViolation();
        }

        return Optional.ofNullable(password).isPresent()
                && passwordPattern.matcher(password).matches();
    }
}
