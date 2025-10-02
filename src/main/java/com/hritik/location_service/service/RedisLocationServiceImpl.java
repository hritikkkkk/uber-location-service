package com.hritik.location_service.service;

import com.hritik.location_service.dto.DriverLocationDto;
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

@Slf4j
@Service
public class RedisLocationServiceImpl implements LocationService {

    private static final String DRIVER_GEO_OPS_KEY = "drivers";

    private static final List<Double> SEARCH_RADII = List.of(2.0, 5.0, 7.0, 10.0);

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${location.required-driver-count}")
    private int requiredDriverCount;

    public RedisLocationServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Boolean saveDriverLocation(String driverId, Double latitude, Double longitude) {
        try {
            GeoOperations<String, String> geoOps = stringRedisTemplate.opsForGeo();
            geoOps.add(
                    DRIVER_GEO_OPS_KEY,
                    new RedisGeoCommands.GeoLocation<>(
                            driverId,
                            new Point(longitude, latitude))
            );
            log.debug("Driver [{}] location saved: lat={}, lon={}", driverId, latitude, longitude);
            return true;
        } catch (Exception e) {
            log.error("Failed to save location for driver [{}]: {}", driverId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<DriverLocationDto> getNearByDrivers(Double latitude, Double longitude) {
        GeoOperations<String, String> geoOps = stringRedisTemplate.opsForGeo();
        List<DriverLocationDto> foundDrivers = new ArrayList<>();
        Set<String> seenDriverIds = new HashSet<>();
        Point userPoint = new Point(longitude, latitude);

        for (Double radiusKm : SEARCH_RADII) {
            Circle within = new Circle(userPoint, new Distance(radiusKm, Metrics.KILOMETERS));

            RedisGeoCommands.GeoRadiusCommandArgs args =
                    RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                            .includeDistance()
                            .sortAscending();

            GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                    geoOps.radius(DRIVER_GEO_OPS_KEY, within, args);

            if (results == null || results.getContent().isEmpty()) continue;

            for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results.getContent()) {
                if (result == null || result.getContent() == null) continue;

                String driverId = result.getContent().getName();
                if (seenDriverIds.contains(driverId)) continue;

                Double distanceKm = result.getDistance() != null ? result.getDistance().getValue() : null;
                if (distanceKm == null || distanceKm > radiusKm) continue;

                List<Point> positions = geoOps.position(DRIVER_GEO_OPS_KEY, driverId);
                if (positions == null || positions.isEmpty()) continue;

                Point p = positions.get(0);
                DriverLocationDto dto = DriverLocationDto.builder()
                        .driverId(driverId)
                        .latitude(p.getY())
                        .longitude(p.getX())
                        .build();

                foundDrivers.add(dto);
                seenDriverIds.add(driverId);
            }

            if (foundDrivers.size() >= requiredDriverCount) break;
        }

        return foundDrivers;
    }


}
