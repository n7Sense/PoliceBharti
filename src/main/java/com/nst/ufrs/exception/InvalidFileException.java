package com.nst.ufrs.exception;


/**
 * Thrown when the uploaded file is not a valid Excel file
 * or the file is empty / null.
 */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }

    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
