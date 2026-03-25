package com.nst.ufrs.service.impl;

import com.nst.ufrs.domain.Event;
import com.nst.ufrs.dto.ExcelUploadResponse;
import com.nst.ufrs.dto.ExcelUploadResponse.RowError;
import com.nst.ufrs.exception.ExcelParseException;
import com.nst.ufrs.exception.InvalidFileException;
import com.nst.ufrs.repository.EventRepository;
import com.nst.ufrs.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String XLS_CONTENT_TYPE =
            "application/vnd.ms-excel";

    private final EventRepository eventRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public ExcelUploadResponse uploadEventsFromExcel(
            MultipartFile file,
            int name,
            String unit,
            int lap,
            Long eventLocationId
    ) {
        validateFile(file);

        if (eventLocationId == null) {
            throw new IllegalArgumentException("eventLocationId is required from session.");
        }

        List<RowError> errors = new ArrayList<>();
        List<Event> toSave = new ArrayList<>();

        int totalRows = 0;
        int skippedRows = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new ExcelParseException("Excel file has no sheets.");
            }

            log.info("Starting Event Excel upload: filename={}, size={}B, sheet={}",
                    file.getOriginalFilename(), file.getSize(), sheet.getSheetName());

            for (Row row : sheet) {
                int rowIndex = row.getRowNum();

                // Row 0 assumed header → skip
                if (rowIndex == 0) {
                    continue;
                }

                String rfidStr = getStringCellValue(row.getCell(1));
                String runningStr = getStringCellValue(row.getCell(2));
                String startStr = getStringCellValue(row.getCell(3));
                String endStr = getStringCellValue(row.getCell(4));
                String diffStr = getStringCellValue(row.getCell(5));
                String marksStr = getStringCellValue(row.getCell(6));

                boolean allBlank =
                        isBlank(rfidStr) &&
                        isBlank(runningStr) &&
                        isBlank(startStr) &&
                        isBlank(endStr) &&
                        isBlank(diffStr) &&
                        isBlank(marksStr);
                if (allBlank) {
                    skippedRows++;
                    continue;
                }

                totalRows++;
                int displayRow = rowIndex + 1;
                List<RowError> rowErrors = new ArrayList<>();

                Integer rfid = parseInteger(rfidStr, "RFID No", displayRow, rowErrors);
                Integer runningNumber = parseInteger(runningStr, "Chest No", displayRow, rowErrors);
                LocalDateTime startTime = parseDateTime(row.getCell(3), startStr, "Start Time", displayRow, rowErrors);
                LocalDateTime endTime = parseDateTime(row.getCell(4), endStr, "End Time", displayRow, rowErrors);
                Double timeDiff = parseDouble(diffStr, "Time Difference", displayRow, rowErrors);
                Double marks = parseDouble(marksStr, "Mark", displayRow, rowErrors);

                if (!rowErrors.isEmpty()) {
                    errors.addAll(rowErrors);
                    skippedRows++;
                    continue;
                }

                Event event = new Event();
                event.setName(name);
                event.setUnit(unit);
                event.setLap(lap);
                event.setRfidNumber(rfid);
                event.setRunningNumber(runningNumber);
                event.setStartTime(startTime);
                event.setEndTime(endTime);
                event.setTimeDifference(timeDiff);
                event.setMarks(marks);

                // Only set reference id; JPA will use it when persisting
                com.nst.ufrs.domain.EventLocation eventLocationRef = new com.nst.ufrs.domain.EventLocation();
                eventLocationRef.setId(eventLocationId);
                event.setEventLocation(eventLocationRef);

                toSave.add(event);
            }

            if (!toSave.isEmpty()) {
                eventRepository.saveAll(toSave);
            }

            log.info("Event upload completed. totalRows={}, saved={}, skipped={}, errors={}",
                    totalRows, toSave.size(), skippedRows, errors.size());

            return ExcelUploadResponse.builder()
                    .success(true)
                    .message(String.format("Upload complete. %d event records saved successfully.", toSave.size()))
                    .totalRowsRead(totalRows)
                    .savedCount(toSave.size())
                    .skippedCount(skippedRows)
                    .errorCount(errors.size())
                    .errors(errors)
                    .build();

        } catch (InvalidFileException | ExcelParseException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to process Event Excel file: {}", ex.getMessage(), ex);
            throw new ExcelParseException("Failed to process Event Excel file: " + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> searchEvents(Integer runningNumber, int limit, Long eventLocationId) {
        if (eventLocationId == null) {
            throw new IllegalArgumentException("eventLocationId is required from session.");
        }
        int pageSize = Math.max(limit, 1);

        if (runningNumber == null) {
            var pageable = PageRequest.of(0, pageSize);
            return eventRepository.findAllByEventLocationIdOrderByIdDesc(eventLocationId, pageable).getContent();
        }

        List<Event> list = eventRepository.findAllByEventLocationIdAndRunningNumberOrderByIdDesc(eventLocationId, runningNumber);
        if (list.size() > pageSize) {
            return list.subList(0, pageSize);
        }
        return list;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploaded file is empty or missing.");
        }
        String name = file.getOriginalFilename();
        String type = file.getContentType();
        boolean validExt = name != null && (
                name.toLowerCase().endsWith(".xlsx") ||
                name.toLowerCase().endsWith(".xls")
        );
        boolean validType = XLSX_CONTENT_TYPE.equals(type) || XLS_CONTENT_TYPE.equals(type);
        if (!validExt && !validType) {
            throw new InvalidFileException("Only .xls or .xlsx files are accepted. Received: " + type);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        CellType type = cell.getCellType();
        if (type == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (type == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                LocalDateTime dt = toLocalDateTime(cell.getDateCellValue().toInstant());
                return dt.format(DATE_TIME_FORMATTER);
            }
            double numeric = cell.getNumericCellValue();
            if (Math.floor(numeric) == numeric) {
                return String.valueOf((long) numeric);
            }
            return String.valueOf(numeric);
        } else if (type == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (type == CellType.FORMULA) {
            try {
                return cell.getStringCellValue();
            } catch (IllegalStateException ex) {
                try {
                    double numeric = cell.getNumericCellValue();
                    if (Math.floor(numeric) == numeric) {
                        return String.valueOf((long) numeric);
                    }
                    return String.valueOf(numeric);
                } catch (IllegalStateException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private static LocalDateTime parseDateTime(Cell cell,
                                               String textValue,
                                               String field,
                                               int row,
                                               List<RowError> errors) {
        if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return toLocalDateTime(cell.getDateCellValue().toInstant());
        }
        if (textValue == null || textValue.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(textValue.trim(), DATE_TIME_FORMATTER);
        } catch (Exception e) {
            errors.add(RowError.builder()
                    .rowNumber(row)
                    .field(field)
                    .rawValue(textValue)
                    .reason("Invalid datetime format, expected yyyy-MM-dd HH:mm:ss")
                    .build());
            return null;
        }
    }

    private static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private static Integer parseInteger(String val,
                                        String field,
                                        int row,
                                        List<RowError> errors) {
        if (val == null || val.isBlank()) {
            return null;
        }
        String cleaned = val.trim().replaceAll("^'+", "").replaceAll("[^0-9-]", "");
        if (cleaned.isBlank()) {
            errors.add(RowError.builder()
                    .rowNumber(row)
                    .field(field)
                    .rawValue(val)
                    .reason("Invalid numeric value")
                    .build());
            return null;
        }
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException ex) {
            errors.add(RowError.builder()
                    .rowNumber(row)
                    .field(field)
                    .rawValue(val)
                    .reason("Invalid numeric value")
                    .build());
            return null;
        }
    }

    private static Double parseDouble(String val,
                                      String field,
                                      int row,
                                      List<RowError> errors) {
        if (val == null || val.isBlank()) {
            return null;
        }
        String cleaned = val.trim().replaceAll("^'+", "");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException ex) {
            errors.add(RowError.builder()
                    .rowNumber(row)
                    .field(field)
                    .rawValue(val)
                    .reason("Invalid numeric value")
                    .build());
            return null;
        }
    }
}

