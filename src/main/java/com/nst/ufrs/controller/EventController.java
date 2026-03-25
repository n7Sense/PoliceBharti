package com.nst.ufrs.controller;

import com.nst.ufrs.domain.Event;
import com.nst.ufrs.dto.ExcelUploadResponse;
import com.nst.ufrs.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Event Upload", description = "APIs for uploading and querying event results")
public class EventController {

    private final EventService eventService;
    private final HttpSession httpSession;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Upload event Excel file",
            description = "Accepts a .xlsx file with RFID / Chest / Start / End / Time Difference / Mark columns " +
                    "and persists rows into the event table. Sr. No and Sign columns are ignored."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "File processed and event records saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or empty file uploaded"),
            @ApiResponse(responseCode = "415", description = "Unsupported file type (only .xlsx accepted)"),
            @ApiResponse(responseCode = "422", description = "File could not be parsed"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ResponseEntity<ExcelUploadResponse> uploadEvents(
            @Parameter(description = "Excel (.xlsx) file containing event result rows", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Event identifier, e.g. 100m / 200m / 5Km", required = true)
            @RequestParam("eventKey") String eventKey,
            @Parameter(description = "Lap value to store against each row", required = true)
            @RequestParam("lap") Integer lap
    ) {
        EventMapping mapping = mapEventKey(eventKey);
        int lapValue = lap != null ? lap : 0;

        Long eventLocationId = (Long) httpSession.getAttribute("eventLocationId");
        if (eventLocationId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ExcelUploadResponse.builder()
                            .success(false)
                            .message("Session missing eventLocationId. Please login again or contact administrator.")
                            .build());
        }

        log.info("Received Event Excel upload: filename={}, eventKey={}, name={}, unit={}, lap={}",
                file.getOriginalFilename(), eventKey, mapping.name(), mapping.unit(), lapValue);

        ExcelUploadResponse response =
                eventService.uploadEventsFromExcel(file, mapping.name(), mapping.unit(), lapValue, eventLocationId);

        HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Search/list event records for UI")
    public ResponseEntity<List<Event>> listEvents(
            @RequestParam(value = "runningNumber", required = false)
            @Parameter(description = "Filter by Chest/Running Number") Integer runningNumber,
            @RequestParam(value = "limit", required = false, defaultValue = "500")
            @Parameter(description = "Maximum number of rows to return") int limit
    ) {
        Long eventLocationId = (Long) httpSession.getAttribute("eventLocationId");
        if (eventLocationId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.List.of());
        }

        List<Event> events = eventService.searchEvents(runningNumber, limit, eventLocationId);
        return ResponseEntity.ok(events);
    }

    private EventMapping mapEventKey(String eventKey) {
        if (eventKey == null) {
            throw new IllegalArgumentException("eventKey is required");
        }
        String key = eventKey.trim().toLowerCase();
        return switch (key) {
            case "100m" -> new EventMapping(100, "m");
            case "200m" -> new EventMapping(200, "m");
            case "600m" -> new EventMapping(600, "m");
            case "800m" -> new EventMapping(800, "m");
            case "5km" -> new EventMapping(5, "km");
            default -> throw new IllegalArgumentException("Unsupported eventKey: " + eventKey);
        };
    }

    private record EventMapping(int name, String unit) {
    }
}

