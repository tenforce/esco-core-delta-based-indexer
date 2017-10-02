package com.tenforce.esco.exception;

/**
 * Corresponds to an error from part of the client (400)
 */
public class NoDataException extends Exception {

    public NoDataException(String message) {
        super(message);
    }

}
