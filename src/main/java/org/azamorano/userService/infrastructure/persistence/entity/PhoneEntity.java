package org.azamorano.userService.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.util.UUID;

@Entity(name = "phones")
@Data
public class PhoneEntity {

    @Id
    @Column(name = "id")
    private UUID phoneId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "number", nullable = false, length = 30)
    private String number;

    @Column(name = "city_code", nullable = false, length = 10)
    private String cityCode;

    @Column(name = "country_code", nullable = false, length = 10)
    private String countryCode;
}
