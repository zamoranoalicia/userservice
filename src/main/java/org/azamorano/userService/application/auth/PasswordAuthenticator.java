package org.azamorano.userService.application.auth;

import org.azamorano.userService.infrastructure.security.CustomUserDetailsService;
import org.azamorano.userService.presentation.exception.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordAuthenticator implements Authenticator {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public PasswordAuthenticator(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails authenticate(String username, String password) {
        try {
            UserDetails user = userDetailsService.loadUserByUsername(username.toLowerCase());
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new UsernameNotFoundException("Invalid username or password");
            }
            return user;
        } catch (AuthException ae) {
            throw ae;
        } catch (Exception e) {
            throw new AuthException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
    }
}
