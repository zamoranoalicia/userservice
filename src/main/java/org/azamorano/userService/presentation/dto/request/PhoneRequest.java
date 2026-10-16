package org.azamorano.userService.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Data
@Builder
@Value
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class PhoneRequest {

    Long number;

    Integer cityCode;

    Integer countryCode;
}