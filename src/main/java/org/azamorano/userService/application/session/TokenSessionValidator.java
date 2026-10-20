package org.azamorano.userService.application.session;

import java.util.UUID;

public interface TokenSessionValidator {
    boolean isActive(UUID userId, String tokenId);
}
