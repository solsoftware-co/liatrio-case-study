# Parking Garage Management System

A RESTful API for managing parking garage operations including floors, bays, parking spots, and vehicle check-in/check-out.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.5**
- **Spring Data JPA**
- **PostgreSQL 16**
- **Docker & Docker Compose**
- **Maven**
- **Lombok**
- **OpenAPI/Swagger UI**
- **JUnit 5 & Mockito**
- **JaCoCo** (Code Coverage)

## Architecture

### Domain Model

```
Floor (1) ──→ (N) Bay (1) ──→ (N) ParkingSpot
                                      ↓
                                      (1)
                                      ↓
Car (1) ──→ (N) ParkingTransaction ──→ (1) ParkingSpot
```

### Key Design Decisions

- **Optimistic Locking**: `@Version` on ParkingSpot prevents concurrent spot assignments
- **Derived State**: Spot availability calculated from active transactions (no duplicate data)
- **Soft Deletes**: Floors, Bays, and Spots use `active` flag for audit trails
- **Transaction Pattern**: Check-in/check-out creates ParkingTransaction records
- **Auto Car Registration**: New cars automatically created during check-in

### Layered Architecture

```
Controller → Service → Repository → Entity
     ↓          ↓
    DTO    Exception Handling
```

## Quick Start

### Prerequisites

- Docker & Docker Compose
- Java 17+ (for local development)
- Maven 3.6+ (for local development)

### Option 1: Docker (Recommended)

```bash
# Start PostgreSQL + Spring Boot app
docker-compose -f docker-compose.dev.yml up --build

# Application will be available at http://localhost:8080
# PostgreSQL will be available at localhost:5432
```

### Option 2: Local Development

```bash
# Start PostgreSQL only
docker-compose -f docker-compose.dev.yml up postgres -d

# Run Spring Boot locally
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or build and run
./mvnw clean package
java -jar target/parking-garage-0.0.1-SNAPSHOT.jar
```

## API Documentation

### Swagger UI

Access interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI Spec

```
http://localhost:8080/api-docs
```

### Core Endpoints

#### Parking Operations
- `POST /api/parking/check-in` - Check in a car
- `POST /api/parking/check-out` - Check out a car by spot
- `POST /api/parking/check-out/license-plate/{plate}` - Check out by license plate
- `GET /api/parking/transactions/active` - Get active parking sessions

#### Parking Spots
- `GET /api/parking-spots` - List all spots
- `GET /api/parking-spots/available` - List available spots
- `GET /api/parking-spots/occupied` - List occupied spots
- `GET /api/parking-spots/identifier/{id}` - Get spot by identifier
- `POST /api/parking-spots` - Create new spot

#### Floors & Bays
- `GET /api/floors` - List all floors
- `POST /api/floors` - Create floor
- `GET /api/bays/floor/{floorId}` - Get bays by floor
- `POST /api/bays` - Create bay

#### Cars
- `GET /api/cars/parked` - List currently parked cars
- `GET /api/cars/license-plate/{plate}` - Get car by license plate

## Example Usage

### Check In a Car

```bash
curl -X POST http://localhost:8080/api/parking/check-in \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "ABC-123",
    "spotIdentifier": "F1-A-01",
    "make": "Toyota",
    "model": "Camry",
    "color": "Blue"
  }'
```

### Get Available Spots

```bash
curl http://localhost:8080/api/parking-spots/available
```

### Check Out a Car

```bash
curl -X POST http://localhost:8080/api/parking/check-out \
  -H "Content-Type: application/json" \
  -d '{
    "spotIdentifier": "F1-A-01"
  }'
```

## Testing

### Run Unit Tests

```bash
./mvnw test
```

### Run Tests with Coverage

```bash
./mvnw clean test
# View coverage report: target/site/jacoco/index.html
```

### Test Coverage

- **Service Layer Tests**: Mock-based unit tests with Mockito
- **Controller Tests**: `@WebMvcTest` for REST endpoints
- **Integration Tests**: Full Spring context with H2 in-memory database

## Sample Data

When running with `dev` profile, the application seeds:
- 3 Floors (Ground, Second, Third)
- 8 Bays across floors
- 88 Parking spots with mixed types (REGULAR, COMPACT, LARGE, HANDICAP)
- 3 Sample cars
- 2 Active parking sessions
- 1 Completed transaction

## Configuration

### Environment Variables

Create a `.env` file (see `.env.example`):

```properties
DATABASE_URL=jdbc:postgresql://localhost:5432/parking_garage
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
PORT=8080
JPA_DDL_AUTO=update
SPRING_PROFILES_ACTIVE=dev
```

### Profiles

- **dev**: Development mode with sample data seeding
- **test**: Integration testing with H2 database
- **prod**: Production mode (no data seeding)

## Database Schema

### Key Entities

**floors**
- id (PK)
- floor_number (UNIQUE)
- name
- active

**bays**
- id (PK)
- bay_identifier
- name
- floor_id (FK)
- active

**parking_spots**
- id (PK)
- spot_identifier (UNIQUE)
- spot_number
- spot_type (ENUM)
- bay_id (FK)
- active
- version (optimistic locking)

**cars**
- id (PK)
- license_plate (UNIQUE)
- make, model, color

**parking_transactions**
- id (PK)
- car_id (FK)
- parking_spot_id (FK)
- check_in_time
- check_out_time
- notes

## Project Structure

```
src/main/java/com/liatrio/parkinggarage/
├── config/          # Data seeder, configuration
├── controller/      # REST endpoints
├── dto/            # Request/Response objects
├── entity/         # JPA entities
├── exception/      # Custom exceptions & global handler
├── mapper/         # Entity ↔ DTO conversion
├── repository/     # Spring Data JPA repositories
└── service/        # Business logic

src/test/java/
├── controller/     # @WebMvcTest controller tests
├── service/        # Mockito service tests
└── integration/    # Full integration tests
```

## Design Patterns Used

- **DTO Pattern**: Never expose entities directly in API
- **Repository Pattern**: Spring Data JPA abstraction
- **Service Layer**: Business logic separation
- **Exception Handling**: Global `@ControllerAdvice`
- **Builder Pattern**: Lombok `@Builder` for clean object creation
- **Factory Method**: Auto-create cars during check-in

## Business Rules

1. **Spot Assignment**: Only available (active, not occupied) spots can be assigned
2. **Concurrent Safety**: Optimistic locking prevents double-booking
3. **Car Parking**: A car can only be parked in one spot at a time
4. **Check-Out**: Only actively parked cars can be checked out
5. **Soft Deletes**: Floors, bays, and spots are deactivated, not deleted

## Future Enhancements (Discussion Points)

### Scalability
- Redis caching for available spots query
- Read replicas for reporting queries
- Event-driven architecture (Kafka) for transaction events

### Features
- Pricing calculation based on duration
- Reserved parking for specific license plates
- Real-time spot availability via WebSocket
- Mobile app integration

### DevOps
- Kubernetes deployment (Helm charts)
- CI/CD pipeline (GitHub Actions, Jenkins)
- Monitoring (Prometheus + Grafana)
- Distributed tracing (Jaeger)
- Blue-green deployment strategy

### Security
- OAuth2/JWT authentication
- Role-based access control (RBAC)
- API rate limiting
- Audit logging

## Troubleshooting

### Port Already in Use

```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9

# Or change port in .env
PORT=8081
```

### Database Connection Issues

```bash
# Verify PostgreSQL is running
docker ps | grep postgres

# Check logs
docker logs parking-garage-db

# Reset database
docker-compose -f docker-compose.dev.yml down -v
docker-compose -f docker-compose.dev.yml up -d
```

### Build Issues

```bash
# Clean rebuild
./mvnw clean install -DskipTests

# Clear Docker cache
docker system prune -a
```

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

## License

This project is created for interview purposes.

## Author

Casey Ramirez - Liatrio Case Study Interview
