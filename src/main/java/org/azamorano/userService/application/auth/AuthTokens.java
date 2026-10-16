package org.azamorano.userService.application.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthTokens {
    private final String accessToken;
    private final String refreshToken;

    public AuthTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
