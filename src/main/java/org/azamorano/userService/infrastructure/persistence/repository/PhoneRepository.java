package org.azamorano.userService.infrastructure.persistence.repository;

import org.azamorano.userService.infrastructure.persistence.entity.PhoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PhoneRepository extends JpaRepository<PhoneEntity, UUID> {
}
