package com.hritik.location_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

/**
 * DTO representing a driver's location information.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Schema(description = "Driver location information")
public class DriverLocationDto implements Serializable {

    @Schema(description = "Unique driver identifier", example = "DRV-12345")
    @JsonProperty("driver_id")
    private String driverId;

    @Schema(description = "Latitude coordinate", example = "28.6139", minimum = "-90", maximum = "90")
    @JsonProperty("latitude")
    private Double latitude;

    @Schema(description = "Longitude coordinate", example = "77.2090", minimum = "-180", maximum = "180")
    @JsonProperty("longitude")
    private Double longitude;

    @Schema(description = "Distance from search point in kilometers", example = "2.5")
    @JsonProperty("distance_km")
    private Double distanceKm;
}