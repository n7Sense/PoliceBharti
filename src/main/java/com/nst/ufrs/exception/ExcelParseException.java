package com.nst.ufrs.exception;

/**
 * Thrown when a fatal error occurs during Excel parsing
 * (e.g., corrupted file, unreadable workbook).
 */
public class ExcelParseException extends RuntimeException {

    public ExcelParseException(String message) {
        super(message);
    }

    public ExcelParseException(String message, Throwable cause) {
        super(message, cause);
    }
}