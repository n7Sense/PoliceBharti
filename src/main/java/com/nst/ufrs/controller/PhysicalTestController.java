package com.nst.ufrs.controller;

import com.nst.ufrs.dto.PhysicalTestDto;
import com.nst.ufrs.dto.PhysicalTestUpsertRequest;
import com.nst.ufrs.service.PhysicalTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/physical-tests")
@RequiredArgsConstructor
@Tag(name = "Physical Test", description = "APIs for candidate physical test (height/chest/expanded chest)")
public class PhysicalTestController {

    private final PhysicalTestService physicalTestService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch physical test by application number")
    public ResponseEntity<?> getByApplicationNo(@RequestParam("applicationNo") long applicationNo) {
        try {
            PhysicalTestDto dto = physicalTestService.getByApplicationNo(applicationNo);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("message", "Physical test not found for this Application Number."));
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create/update physical test by application number")
    public ResponseEntity<?> upsert(@RequestBody PhysicalTestUpsertRequest request) {
        try {
            PhysicalTestDto saved = physicalTestService.upsert(request);
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "Physical test saved successfully",
                    "data", saved
            ));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("message", "Invalid Application Number. Candidate not found."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping(value = "/report", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Download physical test PDF report")
    public ResponseEntity<?> downloadReport(@RequestParam("applicationNo") long applicationNo) {
        try {
            byte[] pdf = physicalTestService.generatePdfReport(applicationNo);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=physical-test-" + applicationNo + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("message", "Failed to generate PDF"));
        }
    }
}

