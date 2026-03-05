package com.nst.ufrs.controller;

import com.nst.ufrs.domain.BatchMaster;
import com.nst.ufrs.domain.Candidate;
import com.nst.ufrs.dto.BatchCandidateRowDto;
import com.nst.ufrs.dto.BatchListItemDto;
import com.nst.ufrs.repository.BatchMasterRepository;
import com.nst.ufrs.repository.CandidateRepository;
import com.nst.ufrs.service.impl.BatchRecruitmentPdfService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
public class BatchMasterController {

    private final BatchMasterRepository batchRepo;
    private final CandidateRepository candidateRepo;
    private final BatchRecruitmentPdfService pdfService;

    private Long getEventLocationId(HttpSession session) {
        Long id = (Long) session.getAttribute("eventLocationId");
        if (id == null) throw new IllegalStateException("Event location not set. Please log in again.");
        return id;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BatchListItemDto>> listBatches(HttpSession session) {
        Long eventLocationId = getEventLocationId(session);
        List<BatchMaster> batches = batchRepo.findAllForEventLocation(eventLocationId);
        List<BatchListItemDto> out = batches.stream().map(this::toListDto).toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping(value = "/{batchId}/candidates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BatchCandidateRowDto>> listBatchCandidates(
            @PathVariable("batchId") Long batchId,
            HttpSession session
    ) {
        Long eventLocationId = getEventLocationId(session);
        if (batchRepo.findByIdAndEventLocationId(batchId, eventLocationId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Candidate> candidates = candidateRepo.findAllByBatchForEventLocation(batchId, eventLocationId);
        List<BatchCandidateRowDto> out = candidates.stream().map(this::toCandidateDto).toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping(value = "/{batchId}/candidates/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> printBatchCandidatesPdf(
            @PathVariable("batchId") Long batchId,
            HttpSession session
    ) {
        Long eventLocationId = getEventLocationId(session);

        BatchMaster batch = batchRepo.findByIdAndEventLocationId(batchId, eventLocationId).orElse(null);
        if (batch == null) {
            return ResponseEntity.status(404)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Batch not found".getBytes(StandardCharsets.UTF_8));
        }

        List<Candidate> candidates = candidateRepo.findAllByBatchForEventLocation(batchId, eventLocationId);
        List<BatchCandidateRowDto> rows = candidates.stream().map(this::toCandidateDto).toList();

        byte[] pdf = pdfService.generateRecruitmentDocument(batch.getBatchCode(), LocalDate.now(), rows);

        String code = (batch.getBatchCode() == null || batch.getBatchCode().isBlank()) ? "batch" : batch.getBatchCode().trim();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("recruitment-" + code + ".pdf").build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    private BatchListItemDto toListDto(BatchMaster b) {
        return BatchListItemDto.builder()
                .id(b.getId())
                .batchId(b.getBatchId())
                .batchCode(b.getBatchCode())
                .batchName(b.getBatchName())
                .batchSize(parseSize(b.getBatchSize()))
                .assignedCount(b.getAssignedCount() != null ? b.getAssignedCount() : 0)
                .batchStatus(b.getBatchStatus())
                .isLocked(b.getIsLocked())
                .lastUsedDate(b.getLastUsedDate())
                .build();
    }

    private BatchCandidateRowDto toCandidateDto(Candidate c) {
        String name = (safe(c.getFirstName()) + " " + safe(c.getSurname())).trim();
        return BatchCandidateRowDto.builder()
                .id(c.getId())
                .srNo(c.getSrNo())
                .runningNumber(c.getRunningNumber())
                .applicationNo(c.getApplicationNo())
                .tokenNo(c.getTokenNo())
                .name(name)
                .mobileNo(c.getMobileNo())
                .post(c.getPost())
                .category(c.getApplicationCategory())
                .build();
    }

    private static int parseSize(String raw) {
        try {
            return Integer.parseInt(raw == null ? "20" : raw.trim());
        } catch (Exception e) {
            return 20;
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}

