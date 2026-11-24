# Design Specification

## Scope
Only booking conflict prevention and cancellation are implemented. `BookingService` is the service boundary. It uses an in-memory map, as allowed when no starter project is supplied.

## Data model
`Appointment` is an immutable Java record with appointment ID, provider ID, start/end `Instant`, and `AppointmentStatus` (`ACTIVE` or `CANCELLED`). `BookingResult` reports success and a message. Invalid data raises `BookingValidationException`.

## Booking invariant
Two ACTIVE appointments for the same provider must not overlap. The predicate is:
`existing.start < requested.end && requested.start < existing.end`
Intervals are half-open `[start, end)`, allowing adjacency. Different providers do not conflict.

## Validation and time
Inputs require nonblank IDs and ISO-8601 UTC timestamps with `Z` or `+00:00`. Naive and non-zero-offset timestamps are rejected. End must be after start. Appointment IDs are unique and not reused.

## Cancellation
Cancellation changes the stored appointment status to CANCELLED. The record is retained but no longer blocks its interval. Missing and already-cancelled IDs return explicit unsuccessful results without changing state.

## Concurrency and persistence
`book` and `cancel` are synchronized instance methods. The overlap check and insert are atomic among threads sharing one service instance. A JUnit test races two overlapping booking attempts and verifies only one succeeds. This does not coordinate multiple JVMs or hosts. Production deployment would require durable storage and a transaction/locking mechanism enforcing the same invariant across all writers.

## Tradeoffs
The in-memory store is volatile. Provider working hours, maximum duration, past-time rules, API transport, frontend, authentication, notifications, recurring availability, calendar integration, and deployment are out of scope.
