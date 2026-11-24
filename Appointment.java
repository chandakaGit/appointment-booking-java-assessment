package com.candidate.appointment;
import java.time.Instant;
public record Appointment(String appointmentId, String providerId, Instant startUtc, Instant endUtc, AppointmentStatus status) {
    public Appointment cancelled() { return new Appointment(appointmentId, providerId, startUtc, endUtc, AppointmentStatus.CANCELLED); }
}
