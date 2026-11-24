package com.candidate.appointment;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory, process-local booking service. */
public class BookingService {
    private final Map<String, Appointment> appointments = new ConcurrentHashMap<>();

    public synchronized BookingResult book(String appointmentId, String providerId, String startUtc, String endUtc) {
        String id = requireId(appointmentId, "appointmentId");
        String provider = requireId(providerId, "providerId");
        Instant start = parseUtc(startUtc, "startUtc");
        Instant end = parseUtc(endUtc, "endUtc");
        if (!end.isAfter(start)) throw new BookingValidationException("Appointment end must be later than start.");
        if (appointments.containsKey(id)) throw new BookingValidationException("appointmentId already exists: " + id);

        for (Appointment existing : appointments.values()) {
            boolean sameProvider = existing.providerId().equals(provider);
            boolean active = existing.status() == AppointmentStatus.ACTIVE;
            boolean overlaps = existing.startUtc().isBefore(end) && start.isBefore(existing.endUtc());
            if (sameProvider && active && overlaps) {
                return BookingResult.failure("Booking conflicts with active appointment: " + existing.appointmentId(), null);
            }
        }
        Appointment appointment = new Appointment(id, provider, start, end, AppointmentStatus.ACTIVE);
        appointments.put(id, appointment);
        return BookingResult.success("Appointment booked successfully.", appointment);
    }

    public synchronized BookingResult cancel(String appointmentId) {
        String id = requireId(appointmentId, "appointmentId");
        Appointment existing = appointments.get(id);
        if (existing == null) return BookingResult.failure("Appointment not found: " + id, null);
        if (existing.status() == AppointmentStatus.CANCELLED) {
            return BookingResult.failure("Appointment is already cancelled: " + id, existing);
        }
        Appointment cancelled = existing.cancelled();
        appointments.put(id, cancelled);
        return BookingResult.success("Appointment cancelled successfully.", cancelled);
    }

    public synchronized Appointment findById(String appointmentId) {
        return appointments.get(requireId(appointmentId, "appointmentId"));
    }
    public synchronized List<Appointment> findAll() { return List.copyOf(appointments.values()); }

    private static String requireId(String value, String field) {
        if (value == null || value.isBlank()) throw new BookingValidationException(field + " is required.");
        return value.trim();
    }

    private static Instant parseUtc(String value, String field) {
        if (value == null || value.isBlank()) throw new BookingValidationException(field + " is required.");
        try {
            OffsetDateTime parsed = OffsetDateTime.parse(value.trim());
            if (!ZoneOffset.UTC.equals(parsed.getOffset())) {
                throw new BookingValidationException(field + " must use UTC (Z or +00:00).");
            }
            return parsed.toInstant();
        } catch (DateTimeParseException ex) {
            throw new BookingValidationException(field + " must be a valid ISO-8601 UTC timestamp.", ex);
        }
    }
}
