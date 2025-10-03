package com.hritik.location_service.controller;

import com.hritik.location_service.dto.DriverLocationDto;
import com.hritik.location_service.dto.NearbyDriversRequestDto;
import com.hritik.location_service.dto.SaveDriverLocationRequestDto;
import com.hritik.location_service.dto.ApiResponse;
import com.hritik.location_service.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing driver locations.
 * Provides endpoints for saving driver locations and finding nearby drivers.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Location Management", description = "APIs for driver location tracking")
public class LocationController {

    private final LocationService locationService;

    /**
     * Save or update driver location.
     *
     * @param request Driver location data
     * @return Success response
     */
    @PostMapping("/drivers")
    @Operation(summary = "Save driver location", description = "Saves or updates a driver's current location")
    public ResponseEntity<ApiResponse<Void>> saveDriverLocation(
            @Valid @RequestBody SaveDriverLocationRequestDto request) {

        log.info("Saving location for driver: {}", request.getDriverId());

        locationService.saveDriverLocation(
                request.getDriverId(),
                request.getLatitude(),
                request.getLongitude()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver location saved successfully"));
    }

    /**
     * Find nearby drivers within configurable radius.
     *
     * @param request User location coordinates
     * @return List of nearby drivers with their locations
     */
    @PostMapping("/drivers/nearby")
    @Operation(summary = "Find nearby drivers", description = "Returns list of drivers near the specified location")
    public ResponseEntity<ApiResponse<List<DriverLocationDto>>> getNearbyDrivers(
            @Valid @RequestBody NearbyDriversRequestDto request) {

        log.info("Searching for drivers near: lat={}, lon={}",
                request.getLatitude(), request.getLongitude());

        List<DriverLocationDto> drivers = locationService.getNearByDrivers(
                request.getLatitude(),
                request.getLongitude()
        );

        log.info("Found {} nearby drivers", drivers.size());

        return ResponseEntity.ok(
                ApiResponse.success("Nearby drivers retrieved successfully", drivers)
        );
    }

    /**
     * Get specific driver's current location.
     *
     * @param driverId Driver identifier
     * @return Driver's current location
     */
    @GetMapping("/drivers/{driverId}")
    @Operation(summary = "Get driver location", description = "Retrieves a specific driver's current location")
    public ResponseEntity<ApiResponse<DriverLocationDto>> getDriverLocation(
            @PathVariable String driverId) {

        log.info("Fetching location for driver: {}", driverId);

        DriverLocationDto location = locationService.getDriverLocation(driverId);

        return ResponseEntity.ok(
                ApiResponse.success("Driver location retrieved successfully", location)
        );
    }

    /**
     * Remove driver from location tracking.
     *
     * @param driverId Driver identifier
     * @return Success response
     */
    @DeleteMapping("/drivers/{driverId}")
    @Operation(summary = "Delete driver location", description = "Removes a driver from location tracking")
    public ResponseEntity<ApiResponse<Void>> deleteDriverLocation(
            @PathVariable String driverId) {

        log.info("Deleting location for driver: {}", driverId);

        locationService.deleteDriverLocation(driverId);

        return ResponseEntity.ok(
                ApiResponse.success("Driver location deleted successfully")
        );
    }
}