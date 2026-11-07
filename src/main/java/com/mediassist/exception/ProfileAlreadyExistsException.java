package com.mediassist.exception;

/**
 * Exception thrown when attempting to create a profile for a user that already has one.
 */
public class ProfileAlreadyExistsException extends RuntimeException {

    public ProfileAlreadyExistsException(String message) {
        super(message);
    }
}
