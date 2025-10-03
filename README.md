# Location Service

A high-performance location tracking service built with Spring Boot and Redis geospatial features. Designed for real-time driver location tracking and proximity searches.

## Features

- **Real-time Location Tracking**: Save and update driver locations efficiently
- **Proximity Search**: Find nearby drivers using Redis geospatial queries
- **Scalable Architecture**: Handles high-throughput location updates
- **RESTful API**: Clean, well-documented REST endpoints
- **Comprehensive Error Handling**: Proper exception handling with meaningful responses
- **API Documentation**: Interactive Swagger UI
- **Health Monitoring**: Spring Boot Actuator endpoints
- **Production Ready**: Includes logging, validation, and best practices

## Tech Stack

- **Java 19**
- **Spring Boot 3.2.0**
- **Spring Data Redis** with jedis client
- **Redis Geospatial** for location queries
- **Lombok** for reducing boilerplate
- **SpringDoc OpenAPI** for API documentation

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Redis 6.0+ (with geospatial support)

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd location-service
```

### 2. Start Redis

Using Docker:
```bash
docker run -d -p 6379:6379 --name redis redis:latest
```

Or install Redis locally from [redis.io](https://redis.io/download)

### 3. Configure Application

Edit `src/main/resources/application.yml` or set environment variables:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: # optional
```

Environment variables:
```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REQUIRED_DRIVER_COUNT=5
export MAX_SEARCH_RADIUS_KM=15.0
```

### 4. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the JAR
java -jar target/location-service-1.0.0.jar
```

The service will start on `http://localhost:7777`

## API Documentation

Once running, access the Swagger UI at:
```
http://localhost:7777/swagger-ui.html
```

OpenAPI JSON specification:
```
http://localhost:7777/api-docs
```

## API Endpoints

### Save Driver Location

**POST** `/api/v1/locations/drivers`

Request body:
```json
{
  "driver_id": "DRV-12345",
  "latitude": 28.6139,
  "longitude": 77.2090
}
```

Response:
```json
{
  "success": true,
  "message": "Driver location saved successfully",
  "timestamp": "2025-10-03T10:30:00"
}
```

### Find Nearby Drivers

**POST** `/api/v1/locations/drivers/nearby`

Request body:
```json
{
  "latitude": 28.6139,
  "longitude": 77.2090
}
```

Response:
```json
{
  "success": true,
  "message": "Nearby drivers retrieved successfully",
  "data": [
    {
      "driver_id": "DRV-12345",
      "latitude": 28.6150,
      "longitude": 77.2100,
      "distance_km": 0.15
    }
  ],
  "timestamp": "2025-10-03T10:30:00"
}
```

### Get Driver Location

**GET** `/api/v1/locations/drivers/{driverId}`

Response:
```json
{
  "success": true,
  "message": "Driver location retrieved successfully",
  "data": {
    "driver_id": "DRV-12345",
    "latitude": 28.6139,
    "longitude": 77.2090
  },
  "timestamp": "2025-10-03T10:30:00"
}
```

### Delete Driver Location

**DELETE** `/api/v1/locations/drivers/{driverId}`

Response:
```json
{
  "success": true,
  "message": "Driver location deleted successfully",
  "timestamp": "2025-10-03T10:30:00"
}
```

## Configuration

### Application Properties

| Property | Description | Default |
|----------|-------------|---------|
| `spring.redis.host` | Redis server host | localhost |
| `spring.redis.port` | Redis server port | 6379 |
| `location.required-driver-count` | Number of drivers to return | 5 |
| `location.max-search-radius-km` | Maximum search radius in km | 15.0 |

### Search Radius Strategy

The service uses an incremental search strategy with predefined radii:
- 2 km
- 5 km
- 7 km
- 10 km
- 15 km

It searches each radius until it finds the required number of drivers.

## Architecture

### Project Structure

```
src/
├── main/
│   ├── java/com/hritik/location_service/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data transfer objects
│   │   ├── exception/       # Custom exceptions
│   │   ├── service/         # Business logic
│   │   └── LocationServiceApplication.java
│   └── resources/
│       └── application.yml  # Configuration
└── test/                    # Unit tests
```

### Key Design Patterns

- **Service Layer Pattern**: Separation of business logic
- **DTO Pattern**: Clean API contracts
- **Repository Pattern**: Abstracted data access
- **Exception Handling**: Centralized error handling
- **Dependency Injection**: Loose coupling via Spring


## Performance Considerations

- **Async Operations**: Non-blocking Redis operations
- **Efficient Queries**: Redis geospatial indexes for O(log(N)) searches
- **Caching**: Spring caching support configured
- **Compression**: Response compression enabled

## Error Handling

The service provides consistent error responses:

```json
{
  "success": false,
  "message": "Error description",
  "data": {
    "field": "error details"
  },
  "timestamp": "2025-10-03T10:30:00"
}
```

HTTP Status Codes:
- `200 OK`: Successful operation
- `201 Created`: Resource created
- `400 Bad Request`: Validation errors
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server errors

## Logging

Logs are structured with configurable levels:
- **INFO**: General information
- **DEBUG**: Detailed debugging (Redis operations)
- **ERROR**: Error conditions with stack traces

## Best Practices Implemented

1. **Clean Code**: Meaningful names, single responsibility
2. **SOLID Principles**: Proper abstraction and interfaces
3. **Validation**: Input validation with Jakarta Bean Validation
4. **Error Handling**: Comprehensive exception handling
5. **Documentation**: Swagger/OpenAPI documentation
6. **Testing**: Unit tests with high coverage
7. **Logging**: Structured logging with SLF4J
8. **Configuration**: Externalized configuration
9. **Security**: Input sanitization, no SQL injection risks
10. **Performance**: Optimized Redis operations

### Environment Variables

Required for production:
```bash
REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=your-secure-password
SERVER_PORT=7777
REQUIRED_DRIVER_COUNT=5
MAX_SEARCH_RADIUS_KM=15.0
```
