# AI-Assisted Engineering Use Record

## Activity 1 – Requirements decomposition
**Prompt summary:** Decompose the assessment into Java service methods, UTC validation, overlap rule, cancellation, concurrency, tests, and required documents.
**AI summary:** Use an in-memory map, explicit UTC parsing, ACTIVE/CANCELLED state, half-open intervals, atomic check-and-insert, and focused tests.
**Disposition:** Accepted as the task plan.
**Files informed:** `BookingService.java`, model classes, and JUnit tests.

## Activity 2 – Java implementation
**Prompt used:** "Implement a minimal Java 17 appointment booking service with in-memory storage. Reject overlapping active appointments for the same provider; allow adjacency and different providers; support cancellation; validate IDs, explicit UTC timestamps, and positive durations; make booking atomic for concurrent threads."
**AI summary:** Use `Instant`, immutable Java records, synchronized service methods, and a status transition on cancellation.
**Disposition:** Accepted with explicit decisions: only `Z` and `+00:00` are accepted; timezone-free and non-zero offsets are rejected; duplicate IDs are rejected.
**Files informed:** `src/main/java/com/candidate/appointment/`.

## Activity 3 – Test planning
**Prompt used:** "Propose JUnit 5 tests for booking success, overlap, adjacency, different providers, cancellation, invalid inputs, duplicate IDs, and simultaneous overlapping requests."
**AI summary:** Verify each acceptance criterion, ensure rejected requests do not mutate storage, and use latches to coordinate two concurrent attempts.
**Disposition:** Implemented in `BookingServiceTest.java`.
**Files informed:** `src/test/java/com/candidate/appointment/BookingServiceTest.java`.

## Independent verification
Run `mvn test` using JDK 17+ and Maven 3.8+. Inspect the test report and record the actual result before submission. Review strict overlap inequalities, cancellation state, UTC parsing, validation-before-mutation, and synchronized check-plus-insert. Do not claim passing tests unless Maven completes successfully.

## Candidate review
AI output is a proposal, not proof. The candidate must inspect the code, run tests locally, understand the interval predicate, UTC contract, synchronization boundary, and process-local persistence limitation. Record any further edits and rerun tests before submission.
