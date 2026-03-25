package com.nst.ufrs.controller;

import com.nst.ufrs.dto.CandidateMasterRowDto;
import com.nst.ufrs.service.impl.CandidateMasterService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CandidateMasterController {

    private final CandidateMasterService candidateMasterService;
    private final HttpSession httpSession;

    @GetMapping("/candidate-master")
    public String viewCandidateMaster() {
        return "candidate-master";
    }

    @GetMapping(value = "/api/v1/candidates/master", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<CandidateMasterRowDto>> list(
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(value = "statusType", required = false) String statusType,
            @RequestParam(value = "approved", required = false) Boolean approved,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {
        Long eventLocationId = (Long) httpSession.getAttribute("eventLocationId");
        Page<CandidateMasterRowDto> result = candidateMasterService.search(
                fromDate, toDate, statusType, approved, page, pageSize, eventLocationId);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/api/v1/candidates/master/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> export(
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam("statusType") String statusType,
            @RequestParam("approved") Boolean approved
    ) {
        Long eventLocationId = (Long) httpSession.getAttribute("eventLocationId");
        boolean includeResultCol = "RESULT_STATUS".equals(statusType);
        byte[] bytes = candidateMasterService.export(fromDate, toDate, statusType, approved, eventLocationId, includeResultCol);

        String statusLabel = approved != null && approved ? "Approved" : "Rejected";
        String fileName = statusType + "-" + statusLabel + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}

