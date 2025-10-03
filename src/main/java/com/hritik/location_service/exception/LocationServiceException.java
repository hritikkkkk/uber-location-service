package com.hritik.location_service.exception;

/**
 * Custom exception for location service operations.
 */
public class LocationServiceException extends RuntimeException {

    public LocationServiceException(String message) {
        super(message);
    }

    public LocationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}