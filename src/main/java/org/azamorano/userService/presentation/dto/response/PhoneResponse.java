package org.azamorano.userService.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PhoneResponse {
    private String number;
    private String cityCode;
    private String countryCode;
}
