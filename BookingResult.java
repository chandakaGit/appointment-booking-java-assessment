package com.candidate.appointment;
public record BookingResult(boolean success, String message, Appointment appointment) {
    public static BookingResult success(String message, Appointment appointment) { return new BookingResult(true, message, appointment); }
    public static BookingResult failure(String message, Appointment appointment) { return new BookingResult(false, message, appointment); }
}
