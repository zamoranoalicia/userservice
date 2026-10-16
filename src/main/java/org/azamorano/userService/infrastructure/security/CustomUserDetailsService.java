package org.azamorano.userService.infrastructure.security;

import org.azamorano.userService.infrastructure.persistence.entity.UserEntity;
import org.azamorano.userService.infrastructure.persistence.entity.UserCredentialsEntity;
import org.azamorano.userService.infrastructure.persistence.repository.UserCredentialsRepository;
import org.azamorano.userService.infrastructure.persistence.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserCredentialsRepository credentialRepository;

    public CustomUserDetailsService(UserRepository userRepository, UserCredentialsRepository credentialRepository) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        UserCredentialsEntity cred = credentialRepository.findById(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        return new CustomUserDetails(user.getId(), user.getEmail(), cred.getPasswordHash(), user.getIsActive());
    }
}
