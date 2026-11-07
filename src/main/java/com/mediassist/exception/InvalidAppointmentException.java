package com.mediassist.exception;

/**
 * Exception thrown when appointment parameters (such as past dates, invalid start/end times) are invalid.
 */
public class InvalidAppointmentException extends RuntimeException {

    public InvalidAppointmentException(String message) {
        super(message);
    }
}

