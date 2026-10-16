package org.azamorano.userService.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.azamorano.userService.presentation.dto.validator.ValidEmail;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotNull
    @ValidEmail
    String username;

    @NotNull
    @NotEmpty
    String password;
}