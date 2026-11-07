package com.mediassist.exception;

/**
 * Exception thrown when a doctor does not have any availability schedule configured.
 */
public class DoctorScheduleNotFoundException extends RuntimeException {

    public DoctorScheduleNotFoundException(String message) {
        super(message);
    }
}

