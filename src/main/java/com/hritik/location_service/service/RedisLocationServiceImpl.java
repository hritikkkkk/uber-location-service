package com.hritik.location_service.service;

import com.hritik.location_service.dto.DriverLocationDto;
import com.hritik.location_service.exception.DriverNotFoundException;
import com.hritik.location_service.exception.LocationServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Redis-based implementation of LocationService.
 * Uses Redis geospatial commands for efficient location queries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLocationServiceImpl implements LocationService {

    private static final String DRIVER_GEO_KEY = "drivers:locations";
    private static final List<Double> DEFAULT_SEARCH_RADII = List.of(2.0, 5.0, 7.0, 10.0, 15.0);

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${location.required-driver-count:5}")
    private int requiredDriverCount;

    @Value("${location.max-search-radius-km:15.0}")
    private double maxSearchRadiusKm;

    @Override
    public void saveDriverLocation(String driverId, Double latitude, Double longitude) {
        validateCoordinates(latitude, longitude);

        try {
            GeoOperations<String, String> geoOps = stringRedisTemplate.opsForGeo();

            Long result = geoOps.add(
                    DRIVER_GEO_KEY,
                    new Point(longitude, latitude),
                    driverId
            );

            if (result != null && result > 0) {
                log.info("Driver location saved: driverId={}, lat={}, lon={}",
                        driverId, latitude, longitude);
            } else {
                log.info("Driver location updated: driverId={}, lat={}, lon={}",
                        driverId, latitude, longitude);
            }

        } catch (Exception e) {
            log.error("Failed to save location for driver {}: {}", driverId, e.getMessage(), e);
            throw new LocationServiceException("Failed to save driver location", e);
        }
    }

    @Override
    public List<DriverLocationDto> getNearByDrivers(Double latitude, Double longitude) {
        validateCoordinates(latitude, longitude);

        try {
            GeoOperations<String, String> geoOps = stringRedisTemplate.opsForGeo();
            Point searchPoint = new Point(longitude, latitude);

            List<DriverLocationDto> foundDrivers = new ArrayList<>();
            Set<String> seenDriverIds = new HashSet<>();

            // Search with increasing radius until we find enough drivers
            for (Double radiusKm : DEFAULT_SEARCH_RADII) {
                if (radiusKm > maxSearchRadiusKm) {
                    break;
                }

                if (foundDrivers.size() >= requiredDriverCount) {
                    break;
                }

                List<DriverLocationDto> driversInRadius = searchDriversInRadius(
                        geoOps, searchPoint, radiusKm, seenDriverIds
                );

                foundDrivers.addAll(driversInRadius);
            }

            log.info("Found {} drivers near location: lat={}, lon={}",
                    foundDrivers.size(), latitude, longitude);

            return foundDrivers;

        } catch (Exception e) {
            log.error("Failed to find nearby drivers: {}", e.getMessage(), e);
            throw new LocationServiceException("Failed to retrieve nearby drivers", e);
        }
    }

    @Override
    public DriverLocationDto getDriverLocation(String driverId) {
        try {
            GeoOperations<String, String> geoOps = stringRedisTemplate.opsForGeo();
            List<Point> positions = geoOps.position(DRIVER_GEO_KEY, driverId);

            if (positions == null || positions.isEmpty() || positions.get(0) == null) {
                throw new DriverNotFoundException("Driver not found: " + driverId);
            }

            Point position = positions.get(0);

            return DriverLocationDto.builder()
                    .driverId(driverId)
                    .latitude(position.getY())
                    .longitude(position.getX())
                    .build();

        } catch (DriverNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get location for driver {}: {}", driverId, e.getMessage(), e);
            throw new LocationServiceException("Failed to retrieve driver location", e);
        }
    }

    @Override
    public void deleteDriverLocation(String driverId) {
        try {
            GeoOperations<String, String> geoOps = stringRedisTemplate.opsForGeo();
            Long removed = geoOps.remove(DRIVER_GEO_KEY, driverId);

            if (removed != null && removed > 0) {
                log.info("Driver location deleted: driverId={}", driverId);
            } else {
                log.warn("Driver location not found for deletion: driverId={}", driverId);
            }

        } catch (Exception e) {
            log.error("Failed to delete location for driver {}: {}", driverId, e.getMessage(), e);
            throw new LocationServiceException("Failed to delete driver location", e);
        }
    }

    /**
     * Search for drivers within a specific radius.
     */
    private List<DriverLocationDto> searchDriversInRadius(
            GeoOperations<String, String> geoOps,
            Point searchPoint,
            Double radiusKm,
            Set<String> seenDriverIds) {

        List<DriverLocationDto> drivers = new ArrayList<>();

        Circle searchArea = new Circle(searchPoint, new Distance(radiusKm, Metrics.KILOMETERS));

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending()
                .limit(requiredDriverCount * 2); // Get more than needed to account for duplicates

        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                geoOps.radius(DRIVER_GEO_KEY, searchArea, args);

        if (results == null) {
            return drivers;
        }

        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
            if (result == null || result.getContent() == null) {
                continue;
            }

            RedisGeoCommands.GeoLocation<String> location = result.getContent();
            String driverId = location.getName();

            // Skip if already seen
            if (seenDriverIds.contains(driverId)) {
                continue;
            }

            Point point = location.getPoint();
            Double distance = result.getDistance() != null ? result.getDistance().getValue() : null;

            // Validate distance is within radius
            if (distance == null || distance > radiusKm) {
                continue;
            }

            DriverLocationDto dto = DriverLocationDto.builder()
                    .driverId(driverId)
                    .latitude(point.getY())
                    .longitude(point.getX())
                    .distanceKm(Math.round(distance * 100.0) / 100.0)
                    .build();

            drivers.add(dto);
            seenDriverIds.add(driverId);

            // Stop if we have enough drivers
            if (drivers.size() >= requiredDriverCount) {
                break;
            }
        }

        return drivers;
    }

    /**
     * Validate latitude and longitude coordinates.
     */
    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Latitude and longitude cannot be null");
        }

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }

        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }
}