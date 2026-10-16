package org.azamorano.userService.application.auth;

import org.springframework.security.core.userdetails.UserDetails;

public interface Authenticator {
    UserDetails authenticate(String username, String password);
}
