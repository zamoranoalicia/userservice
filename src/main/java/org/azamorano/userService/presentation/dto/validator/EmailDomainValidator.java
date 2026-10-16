package org.azamorano.userService.presentation.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Component
@Slf4j
public class EmailDomainValidator implements ConstraintValidator<ValidEmail, String> {

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final String DOMAIN_CONSTRAINT_NOT_DEFINED = "Domain constraint not defined";
    private static final String AT_SIGN = "@";

    private String domain;
    private String errorMessage;
    private String domainErrorMessage;


    public EmailDomainValidator(@Value("${config.user.domain.value}") String domain,
                                @Value("${config.user.email.errorMessage}") String errorMessage,
                                @Value("${config.user.domain.errorMessage}") String domainErrorMessage) {
        this.domain = domain;
        this.errorMessage = errorMessage;
        this.domainErrorMessage = domainErrorMessage;
    }

    @Override
    public void initialize(ValidEmail data) {
        if (StringUtils.isEmpty(domain)) {
            log.error(DOMAIN_CONSTRAINT_NOT_DEFINED);
        }
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        if (!EMAIL_REGEX.matcher(email).matches()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    errorMessage).addConstraintViolation();
            return false;
        }

        return isValidDomain(email, constraintValidatorContext);
    }

    private boolean isValidDomain(String email, ConstraintValidatorContext constraintValidatorContext) {
        String emailDomain = email.substring(email.lastIndexOf(AT_SIGN) + 1);
        if (!emailDomain.equalsIgnoreCase(domain)) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    domainErrorMessage).addConstraintViolation();
            return false;
        }
        return true;
    }
}