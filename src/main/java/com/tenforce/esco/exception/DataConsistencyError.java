package com.tenforce.esco.exception;

/**
 * Corresponds to a data error that is inconsistent with the model.
 * This exception is thrown as a fatal error for inmediate data correction
 */
public class DataConsistencyError extends RuntimeException {

    public DataConsistencyError(String message) {
        super(message);
    }

}
