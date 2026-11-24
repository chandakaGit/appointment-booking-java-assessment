package com.candidate.appointment;
public class BookingValidationException extends IllegalArgumentException {
    public BookingValidationException(String message) { super(message); }
    public BookingValidationException(String message, Throwable cause) { super(message, cause); }
}
