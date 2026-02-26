package com.nst.ufrs.dto;

import lombok.*;
import java.util.List;

/**
 * DTO returned after processing an Excel upload.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelUploadResponse {

    private boolean success;
    private String message;
    private int totalRowsRead;
    private int savedCount;
    private int skippedCount;
    private int errorCount;
    private List<RowError> errors;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowError {
        private int rowNumber;
        private String field;
        private String rawValue;
        private String reason;
    }
}
