package com.mediassist.exception;

/**
 * Exception thrown when an appointment booking or status transition conflicts
 * with an existing appointment schedule or overlapping time slot.
 */
public class AppointmentConflictException extends RuntimeException {

    public AppointmentConflictException(String message) {
        super(message);
    }
}

