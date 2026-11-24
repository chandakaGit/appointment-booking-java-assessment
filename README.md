# Appointment Booking Service (Java)

Minimal Java 17 implementation of the Charles Schwab appointment booking assessment, using an in-memory store because no starter project was supplied.

## Requirements
- JDK 17+
- Maven 3.8+

Check: `java -version` and `mvn -version`.

## Run tests
From this directory, run:
```bash
mvn test
```

Build:
```bash
mvn clean package
```

## Example
```java
BookingService service = new BookingService();
BookingResult booked = service.book("appt-1", "provider-1",
    "2026-11-24T10:00:00Z", "2026-11-24T11:00:00Z");
BookingResult cancelled = service.cancel("appt-1");
```
Classes are in `com.candidate.appointment`.

## Assumptions
- Timestamp inputs must be ISO-8601 UTC with `Z` or `+00:00`; naive and non-zero offsets are rejected.
- Intervals are half-open `[start, end)`, so adjacent appointments are permitted.
- Only ACTIVE appointments block a booking.
- Cancelled records are retained; IDs cannot be reused.
- Invalid input throws `BookingValidationException`; conflicts and unsuccessful cancellations return `success=false`.

## Concurrency and limitations
Synchronized service methods make booking check-and-insert atomic for threads sharing this instance. Data is process-local and volatile. Multi-JVM production use requires durable storage and cross-instance transaction/locking. Frontend, authentication, notifications, recurring availability, calendar integration, and deployment are out of scope.
