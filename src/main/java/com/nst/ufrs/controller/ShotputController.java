package com.nst.ufrs.controller;

import com.nst.ufrs.dto.ShotputDto;
import com.nst.ufrs.dto.ShotputUpsertRequest;
import com.nst.ufrs.service.impl.ShotputService;
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
@RequestMapping("/api/v1/shotput")
@RequiredArgsConstructor
@Tag(name = "Shotput", description = "APIs for candidate shotput attempts and marks")
public class ShotputController {

    private final ShotputService shotputService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch shotput record by application number")
    public ResponseEntity<?> getByApplicationNo(@RequestParam("applicationNo") long applicationNo) {
        try {
            ShotputDto dto = shotputService.getByApplicationNo(applicationNo);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("message", "Shotput record not found for this Application Number."));
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create/update shotput record by application number")
    public ResponseEntity<?> upsert(@RequestBody ShotputUpsertRequest request) {
        try {
            ShotputDto saved = shotputService.upsert(request);
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "Shotput record saved successfully",
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
    @Operation(summary = "Download shotput PDF report")
    public ResponseEntity<?> downloadReport(@RequestParam("applicationNo") long applicationNo) {
        try {
            byte[] pdf = shotputService.generatePdfReport(applicationNo);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=shotput-" + applicationNo + ".pdf")
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

