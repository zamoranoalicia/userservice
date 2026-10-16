package org.azamorano.userService.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import org.azamorano.userService.presentation.dto.validator.Password;
import org.azamorano.userService.presentation.dto.validator.ValidEmail;

import java.util.List;

@Data
@Value
@Builder
@AllArgsConstructor
public class UserCreateRequest {

    @NotNull
    @NotEmpty(message = "name value must not be empty")
    String name;

    @NotNull
    @NotEmpty(message = "email value must not be empty")
    @ValidEmail
    String email;

    @NotNull
    @NotEmpty(message = "password value must not be empty")
    @Password
    String password;

    List<PhoneRequest> phones;
}