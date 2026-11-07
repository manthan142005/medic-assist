package com.mediassist.exception;

/**
 * Exception thrown when an invalid appointment status transition is attempted
 * (such as accepting an already completed appointment or cancelling a rejected one).
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}

