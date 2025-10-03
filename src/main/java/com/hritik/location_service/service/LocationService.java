package com.hritik.location_service.service;

import com.hritik.location_service.dto.DriverLocationDto;

import java.util.List;

/**
 * Service interface for location operations.
 * Defines contract for driver location management.
 */
public interface LocationService {

    /**
     * Save or update driver's location.
     *
     * @param driverId  Unique driver identifier
     * @param latitude  Latitude coordinate
     * @param longitude Longitude coordinate
     */
    void saveDriverLocation(String driverId, Double latitude, Double longitude);

    /**
     * Find drivers near a specific location.
     *
     * @param latitude  Latitude coordinate of search point
     * @param longitude Longitude coordinate of search point
     * @return List of nearby drivers with their locations
     */
    List<DriverLocationDto> getNearByDrivers(Double latitude, Double longitude);

    /**
     * Get a specific driver's current location.
     *
     * @param driverId Unique driver identifier
     * @return Driver's location information
     */
    DriverLocationDto getDriverLocation(String driverId);

    /**
     * Remove driver's location from tracking.
     *
     * @param driverId Unique driver identifier
     */
    void deleteDriverLocation(String driverId);
}