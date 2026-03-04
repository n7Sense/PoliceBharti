package com.nst.ufrs.controller;

import com.nst.ufrs.dto.*;
import com.nst.ufrs.service.CandidateService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;

/**
 * REST Controller for Police Bharti Candidate Excel Upload.
 *
 * <p>Endpoint: POST /api/v1/candidates/upload
 */
@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Candidate Upload", description = "APIs for uploading Police Bharti candidate data via Excel")
public class CandidateController {

    private final CandidateService candidateService;

    /**
     * Upload an Excel (.xlsx) file containing candidate records.
     *
     * <p>Curl example:
     * <pre>
     *   curl -X POST http://localhost:8080/api/v1/candidates/upload \
     *        -F "file=@PoliceBharti_Candidate.xlsx"
     * </pre>
     *
     * @param file the Excel file (multipart/form-data)
     * @return ResponseEntity with upload summary and any parsing errors
     */
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Upload candidate Excel file",
            description = "Accepts a .xlsx file, parses all candidate rows, and persists them to MySQL. " +
                    "Header row is automatically skipped. Numeric parsing errors are captured per-row."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "File processed and records saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or empty file uploaded"),
            @ApiResponse(responseCode = "415", description = "Unsupported file type (only .xlsx accepted)"),
            @ApiResponse(responseCode = "422", description = "File could not be parsed"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ResponseEntity<ExcelUploadResponse> uploadCandidates(
            @Parameter(description = "Excel (.xlsx) file containing candidate records", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("Received Excel upload request: filename={}, size={}B", file.getOriginalFilename(), file.getSize());

        ExcelUploadResponse response = candidateService.uploadCandidatesFromExcel(file);

        HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Health-check endpoint for the candidate upload module.
     */
    @GetMapping("/health")
    @Operation(summary = "Module health check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Police Bharti Candidate Upload Module is running.");
    }

    /**
     * Search / list candidates for admin UI table.
     *
     * All parameters are optional. If none are provided, returns recent candidates.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Search/list candidates for admin UI")
    public ResponseEntity<java.util.List<CandidateListItemDto>> listCandidates(
            @RequestParam(value = "applicationNo", required = false) String applicationNo,
            @RequestParam(value = "mobileNo", required = false) Long mobileNo,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "limit", required = false, defaultValue = "200") int limit
    ) {
        var result = candidateService.searchCandidates(applicationNo, mobileNo, name, limit);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch candidate details by application number")
    public ResponseEntity<?> getCandidateDetails(@RequestParam("applicationNo") long applicationNo) {
        try {
            CandidateDetailsDto dto = candidateService.getCandidateDetailsByApplicationNo(applicationNo);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("message", "Invalid Application Number. Candidate not found."));
        }
    }

    @GetMapping(value = "/verification-data", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch stored photo + biometrics for verification")
    public ResponseEntity<?> getVerificationData(@RequestParam("applicationNo") long applicationNo) {
        try {
            CandidateVerificationDataDto dto = candidateService.getCandidateVerificationData(applicationNo);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("message", "Invalid Application Number. Candidate not found."));
        }
    }

    @PostMapping(value = "/enroll", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Save candidate photo and biometrics")
    public ResponseEntity<?> enrollCandidate(@RequestBody CandidateEnrollmentRequest request) {
        try {
            candidateService.enrollCandidate(request);
            return ResponseEntity.ok(java.util.Map.of("message", "Candidate Added Successfully"));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("message", "Invalid Application Number. Candidate not found."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }
}