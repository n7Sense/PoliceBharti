package com.nst.ufrs.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Utility class for safely reading Apache POI Cell values.
 * All methods handle null cells and type mismatches gracefully.
 */
@Slf4j
public final class ExcelCellReader {

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    private ExcelCellReader() {}

    /**
     * Read a cell as String. Handles all cell types.
     */
    public static String readString(Cell cell) {
        if (cell == null) return null;
        String raw = DATA_FORMATTER.formatCellValue(cell).trim();
        return raw.isEmpty() ? null : raw;
    }

    /**
     * Read a cell as Long (for TokenNo, ApplicationNo, MobileNo).
     * Strips leading apostrophes (Excel text prefix trick) and parses safely.
     *
     * @return parsed Long, or null if blank/invalid
     */
    public static Long readLong(Cell cell, String fieldName, int rowNum,
                                java.util.List<com.nst.ufrs.dto.ExcelUploadResponse.RowError> errors) {
        if (cell == null) return null;

        String raw = DATA_FORMATTER.formatCellValue(cell).trim();
        if (raw.isEmpty()) return null;

        // Strip leading apostrophe used in Excel to force text format (e.g., '25106060000024)
        if (raw.startsWith("'")) {
            raw = raw.substring(1).trim();
        }

        try {
            // Handle numeric cells stored as double (e.g., 1.10606E+14)
            if (cell.getCellType() == CellType.NUMERIC) {
                return (long) cell.getNumericCellValue();
            }
            // Parse from string representation
            return Long.parseLong(raw.replaceAll("[^0-9]", "").trim());
        } catch (NumberFormatException ex) {
            log.warn("Row {}: Invalid numeric value for field '{}': '{}'", rowNum, fieldName, raw);
            if (errors != null) {
                errors.add(com.nst.ufrs.dto.ExcelUploadResponse.RowError.builder()
                        .rowNumber(rowNum)
                        .field(fieldName)
                        .rawValue(raw)
                        .reason("Invalid numeric value; expected Long")
                        .build());
            }
            return null;
        }
    }

    /**
     * Read a cell as Integer.
     */
    public static Integer readInteger(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            }
            String raw = DATA_FORMATTER.formatCellValue(cell).trim();
            return raw.isEmpty() ? null : Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Read a cell as Double.
     */
    public static Double readDouble(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            }
            String raw = DATA_FORMATTER.formatCellValue(cell).trim();
            return raw.isEmpty() ? null : Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Read a cell as LocalDate.
     * Handles both native date cells and numeric serial dates.
     */
    public static LocalDate readDate(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        } catch (Exception ex) {
            log.debug("Could not parse date from cell: {}", ex.getMessage());
        }
        return null;
    }

    /**
     * Safely get a Cell from a row by index (returns null instead of throwing).
     */
    public static Cell getCell(Row row, int colIndex) {
        if (row == null) return null;
        return row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    }
}
