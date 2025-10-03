package com.hritik.location_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO for finding nearby drivers.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(description = "Request to find nearby drivers")
public class NearbyDriversRequestDto {

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Schema(description = "Latitude coordinate of search location", example = "28.6139", required = true)
    @JsonProperty("latitude")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Schema(description = "Longitude coordinate of search location", example = "77.2090", required = true)
    @JsonProperty("longitude")
    private Double longitude;

    @Schema(description = "Maximum search radius in kilometers (optional)", example = "10.0")
    @JsonProperty("max_radius_km")
    private Double maxRadiusKm;
}