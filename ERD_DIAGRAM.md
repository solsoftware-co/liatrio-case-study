# Entity Relationship Diagram (ERD)
## Parking Garage Management System

## Complete ERD with Relationships

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         PARKING GARAGE MANAGEMENT SYSTEM                      │
└──────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────┐
│      Floor          │
├─────────────────────┤
│ 🔑 id (PK)          │
│    floor_number UQ  │◄─ Unique: Only one Floor 1, Floor 2, etc.
│    name             │
│    active           │◄─ Soft delete flag
└──────────┬──────────┘
           │ 1
           │
           │ N
┌──────────▼──────────┐
│      Bay            │
├─────────────────────┤
│ 🔑 id (PK)          │
│ 🔗 floor_id (FK)    │
│    bay_identifier   │◄─ Composite unique with floor_id
│    name             │   (e.g., Floor 1 can have Bay A, Floor 2 can have Bay A)
│    active           │
└──────────┬──────────┘
           │ 1
           │
           │ N
┌──────────▼──────────────┐
│    ParkingSpot          │
├─────────────────────────┤
│ 🔑 id (PK)              │
│ 🔗 bay_id (FK)          │
│ 🔗 spot_type_id (FK) ───────────┐
│    spot_identifier  UQ  │       │
│    spot_number          │◄──────┼─ Composite unique with bay_id
│    active               │       │   (Each bay can have spot "01")
│    version              │◄──────┼─ Optimistic locking (prevents double-booking)
└──────────┬──────────────┘       │
           │ 1                     │ N
           │                       │
           │ N          ┌──────────▼──────────┐
┌──────────▼────────────┤    SpotType         │
│ ParkingTransaction    │├─────────────────────┤
├───────────────────────┤│ 🔑 id (PK)          │
│ 🔑 id (PK)            ││    name UQ          │◄─ REGULAR, COMPACT, LARGE, HANDICAP, EV
│ 🔗 car_id (FK)        ││    description      │
│ 🔗 spot_id (FK)       ││    active           │
│    check_in_time      │└─────────────────────┘
│    check_out_time     │◄─ NULL = currently parked
│    parking_fee        │◄─ Calculated on checkout
│    notes              │
└───────────▲───────────┘
            │ N
            │
            │ 1
┌───────────┴───────────┐
│       Car             │
├───────────────────────┤
│ 🔑 id (PK)            │
│    license_plate  UQ  │
│    make               │
│    model              │
│    color              │
└───────────────────────┘

Legend:
🔑 = Primary Key
🔗 = Foreign Key
UQ = Unique Constraint
```

---

## Detailed Relationship Descriptions

### 1. Floor ↔ Bay (One-to-Many)
**Cardinality:** 1 Floor → N Bays

**Relationship:** A floor contains multiple bays (sections/areas).

**Foreign Key:** `bay.floor_id` → `floor.id`

**Cascade Behavior:**
- **Soft Delete:** Marking floor as inactive logically deactivates associated bays
- **Query Pattern:** `SELECT * FROM bays WHERE floor_id = ? AND active = true`

**Business Rule:** Each bay must belong to exactly one floor.

---

### 2. Bay ↔ ParkingSpot (One-to-Many)
**Cardinality:** 1 Bay → N ParkingSpots

**Relationship:** A bay contains multiple individual parking spots.

**Foreign Key:** `parking_spot.bay_id` → `bay.id`

**Unique Constraint:** `(bay_id, spot_number)` - Prevents duplicate spot numbers within a bay

**Example:**
```
Bay A on Floor 1:
- Spot 01, 02, 03... (spot_identifier: F1-A-01, F1-A-02, F1-A-03)

Bay A on Floor 2:
- Spot 01, 02, 03... (spot_identifier: F2-A-01, F2-A-02, F2-A-03)
```

---

### 3. SpotType ↔ ParkingSpot (One-to-Many)
**Cardinality:** 1 SpotType → N ParkingSpots

**Relationship:** Multiple spots can share the same type classification.

**Foreign Key:** `parking_spot.spot_type_id` → `spot_type.id`

**Design Decision:** Entity (not enum) for runtime extensibility

**Example:**
```
SpotType "REGULAR" (id=1):
  - Spot F1-A-01
  - Spot F1-A-02
  - Spot F2-B-05

SpotType "ELECTRIC_VEHICLE" (id=5):
  - Spot F1-EV-01
  - Spot F2-EV-01
```

**Benefits:**
- Add new types without code deployment
- Attach metadata (dimensions, pricing multipliers)
- Support multi-tenant configurations

---

### 4. ParkingSpot ↔ ParkingTransaction (One-to-Many)
**Cardinality:** 1 ParkingSpot → N ParkingTransactions (over time)

**Relationship:** Historical record of all parking sessions at a spot.

**Foreign Key:** `parking_transaction.spot_id` → `parking_spot.id`

**Active Transaction:** `WHERE check_out_time IS NULL`

**Derived State:**
```java
// Spot is occupied if there's an active transaction
@Transient
public boolean isOccupied() {
    return transactions.stream()
        .anyMatch(t -> t.getCheckOutTime() == null);
}
```

**Concurrency Control:**
```java
@Version
private Long version;  // On ParkingSpot entity
```
- Hibernate checks version before UPDATE
- Prevents double-booking race conditions
- Transaction fails if version changed → client retries

---

### 5. Car ↔ ParkingTransaction (One-to-Many)
**Cardinality:** 1 Car → N ParkingTransactions

**Relationship:** Customer history - all parking sessions for a vehicle.

**Foreign Key:** `parking_transaction.car_id` → `car.id`

**Use Cases:**
- Loyalty programs (frequent parker discounts)
- Billing history
- Dispute resolution
- Analytics (average session duration per customer)

**Auto-Registration Pattern:**
```java
// During check-in, create car if doesn't exist
Car car = carRepository.findByLicensePlate(plate)
    .orElseGet(() -> carRepository.save(
        Car.builder()
            .licensePlate(plate)
            .make(make)
            .model(model)
            .build()
    ));
```

---

## Database Schema (PostgreSQL)

### Tables

```sql
-- 1. Floors
CREATE TABLE floors (
    id              BIGSERIAL PRIMARY KEY,
    floor_number    INTEGER UNIQUE NOT NULL,
    name            VARCHAR(255) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT true
);

-- 2. Bays
CREATE TABLE bays (
    id              BIGSERIAL PRIMARY KEY,
    floor_id        BIGINT NOT NULL REFERENCES floors(id),
    bay_identifier  VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT true,
    UNIQUE(floor_id, bay_identifier)
);

-- 3. Spot Types
CREATE TABLE spot_types (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) UNIQUE NOT NULL,
    description     VARCHAR(500),
    active          BOOLEAN NOT NULL DEFAULT true
);

-- 4. Parking Spots
CREATE TABLE parking_spots (
    id                  BIGSERIAL PRIMARY KEY,
    bay_id              BIGINT NOT NULL REFERENCES bays(id),
    spot_type_id        BIGINT NOT NULL REFERENCES spot_types(id),
    spot_identifier     VARCHAR(255) UNIQUE NOT NULL,
    spot_number         VARCHAR(255) NOT NULL,
    active              BOOLEAN NOT NULL DEFAULT true,
    version             BIGINT,  -- Optimistic locking
    UNIQUE(bay_id, spot_number)
);

-- 5. Cars
CREATE TABLE cars (
    id              BIGSERIAL PRIMARY KEY,
    license_plate   VARCHAR(255) UNIQUE NOT NULL,
    make            VARCHAR(255),
    model           VARCHAR(255),
    color           VARCHAR(255)
);

-- 6. Parking Transactions
CREATE TABLE parking_transactions (
    id              BIGSERIAL PRIMARY KEY,
    car_id          BIGINT NOT NULL REFERENCES cars(id),
    parking_spot_id BIGINT NOT NULL REFERENCES parking_spots(id),
    check_in_time   TIMESTAMP NOT NULL,
    check_out_time  TIMESTAMP,
    parking_fee     DOUBLE PRECISION,
    notes           TEXT
);

-- Indexes for performance
CREATE INDEX idx_transaction_active ON parking_transactions(parking_spot_id, check_out_time);
CREATE INDEX idx_transaction_car ON parking_transactions(car_id);
CREATE INDEX idx_spots_bay ON parking_spots(bay_id);
CREATE INDEX idx_bays_floor ON bays(floor_id);
```

---

## Key Query Patterns

### 1. Find Available Spots
```sql
SELECT ps.* 
FROM parking_spots ps
WHERE ps.active = true
  AND NOT EXISTS (
    SELECT 1 FROM parking_transactions pt
    WHERE pt.parking_spot_id = ps.id
      AND pt.check_out_time IS NULL
  )
ORDER BY ps.id;
```

### 2. Check if Car is Currently Parked
```sql
SELECT pt.*, ps.spot_identifier
FROM parking_transactions pt
JOIN parking_spots ps ON pt.parking_spot_id = ps.id
WHERE pt.car_id = ?
  AND pt.check_out_time IS NULL;
```

### 3. Get Occupancy Rate for Floor
```sql
SELECT 
    f.floor_number,
    COUNT(ps.id) as total_spots,
    COUNT(CASE WHEN pt.id IS NOT NULL THEN 1 END) as occupied_spots,
    ROUND(COUNT(CASE WHEN pt.id IS NOT NULL THEN 1 END)::numeric / 
          COUNT(ps.id)::numeric * 100, 2) as occupancy_rate
FROM floors f
JOIN bays b ON f.id = b.floor_id
JOIN parking_spots ps ON b.id = ps.bay_id
LEFT JOIN parking_transactions pt ON ps.id = pt.parking_spot_id 
    AND pt.check_out_time IS NULL
WHERE f.id = ?
  AND ps.active = true
GROUP BY f.id, f.floor_number;
```

---

## Design Patterns Illustrated

### 1. Optimistic Locking Pattern
**Problem:** Two users try to book the same spot simultaneously

**Solution:**
```
User A reads ParkingSpot (version = 1)
User B reads ParkingSpot (version = 1)

User A updates → version = 2 ✅
User B tries to update → version mismatch ❌ (expected 1, found 2)
  → Throws OptimisticLockException
  → Client retries with fresh data
```

### 2. Soft Delete Pattern
**Problem:** Need audit trails, can't permanently delete

**Solution:**
- Add `active` boolean flag to entities
- Queries filter by `active = true`
- "Delete" operations set `active = false`
- Historical data preserved for compliance

### 3. Derived State Pattern
**Problem:** `occupied` status could desync with transactions

**Solution:**
- Calculate `occupied` from transactions in real-time
- No duplicate state to maintain
- Single source of truth
- Use `@Transient` for computed properties

---

## Scalability Considerations

### Current Design (MVP):
- Single PostgreSQL database
- Suitable for: 1-10 garages, <10,000 spots
- ACID transactions guarantee consistency

### Future Scaling Path:

**Read Replicas:**
```
Master (writes) → Replica 1 (analytics queries)
                → Replica 2 (availability API)
```

**Database Sharding (Multi-Tenancy):**
```
Shard by garage_id:
- Database 1: Garages 1-100
- Database 2: Garages 101-200
```

**Caching Layer:**
```
Redis Cache: Available spots (TTL: 30 seconds)
- High-read endpoint optimization
- Invalidate on check-in/check-out
```

**Event-Driven Architecture:**
```
Check-in/out → Kafka Event → Multiple Consumers:
  - Billing Service
  - Notification Service
  - Analytics Service
  - Availability Cache Updater
```
