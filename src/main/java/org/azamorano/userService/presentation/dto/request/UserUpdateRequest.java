package org.azamorano.userService.presentation.dto.request;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.azamorano.userService.presentation.dto.validator.ValidEmail;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String name;

    @ValidEmail
    private String email;

    @Valid
    private List<PhoneRequest> phones;
}
