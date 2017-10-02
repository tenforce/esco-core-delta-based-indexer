package com.tenforce.esco.exception;

/**
 * Corresponds to an error from part of the client (400)
 */
public class InvalidParameterException extends RuntimeException {

    public InvalidParameterException(String message) {
        super(message);
    }

}
