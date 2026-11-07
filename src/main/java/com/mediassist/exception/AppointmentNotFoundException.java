package com.mediassist.exception;

/**
 * Exception thrown when a requested appointment cannot be found.
 */
public class AppointmentNotFoundException extends RuntimeException {

    public AppointmentNotFoundException(String message) {
        super(message);
    }
}

