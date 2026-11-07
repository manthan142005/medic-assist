package com.mediassist.exception;

/**
 * Exception thrown when a patient profile is requested but does not exist.
 */
public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException(String message) {
        super(message);
    }
}
