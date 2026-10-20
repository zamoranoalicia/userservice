package org.azamorano.userService.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private OffsetDateTime createdAt;
    private OffsetDateTime modifiedAt;
    private OffsetDateTime lastLoginAt;
    private String token; // access token
    private Boolean isActive;
    private List<PhoneResponse> phones;
}
