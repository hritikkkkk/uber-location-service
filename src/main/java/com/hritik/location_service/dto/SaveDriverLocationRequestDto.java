package com.hritik.location_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for saving driver location.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(description = "Request to save driver location")
public class SaveDriverLocationRequestDto {

    @NotBlank(message = "Driver ID is required")
    @Size(min = 3, max = 50, message = "Driver ID must be between 3 and 50 characters")
    @Schema(description = "Unique driver identifier", example = "DRV-12345", required = true)
    @JsonProperty("driver_id")
    private String driverId;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Schema(description = "Latitude coordinate", example = "28.6139", required = true)
    @JsonProperty("latitude")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Schema(description = "Longitude coordinate", example = "77.2090", required = true)
    @JsonProperty("longitude")
    private Double longitude;
}