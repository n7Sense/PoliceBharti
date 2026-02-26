package com.nst.ufrs.exception;

import com.nst.ufrs.dto.ExcelUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Collections;

/**
 * Centralized exception handler for all REST controllers.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ExcelUploadResponse> handleInvalidFile(InvalidFileException ex) {
        log.warn("Invalid file upload attempt: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ExcelUploadResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errors(Collections.emptyList())
                        .build());
    }

    @ExceptionHandler(ExcelParseException.class)
    public ResponseEntity<ExcelUploadResponse> handleExcelParseError(ExcelParseException ex) {
        log.error("Excel parsing failed: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ExcelUploadResponse.builder()
                        .success(false)
                        .message("Excel parsing error: " + ex.getMessage())
                        .errors(Collections.emptyList())
                        .build());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ExcelUploadResponse> handleFileSizeLimit(MaxUploadSizeExceededException ex) {
        log.warn("File size exceeded: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ExcelUploadResponse.builder()
                        .success(false)
                        .message("File size exceeds the maximum allowed limit of 50MB.")
                        .errors(Collections.emptyList())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExcelUploadResponse> handleGenericError(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExcelUploadResponse.builder()
                        .success(false)
                        .message("An unexpected error occurred. Please contact support.")
                        .errors(Collections.emptyList())
                        .build());
    }
}
