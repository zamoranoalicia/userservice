package org.azamorano.userService.infrastructure.persistence.repository;

import org.azamorano.userService.infrastructure.persistence.entity.UserCredentialsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentialsEntity, UUID> {
}
