package com.nst.ufrs.controller;

import com.nst.ufrs.domain.BatchMaster;
import com.nst.ufrs.dto.AssignBatchDto;
import com.nst.ufrs.dto.AssignPageDataDto;
import com.nst.ufrs.dto.AssignRunningNumberResultDto;
import com.nst.ufrs.dto.LockBatchResultDto;
import com.nst.ufrs.repository.BatchMasterRepository;
import com.nst.ufrs.service.AssignRunningNumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/assign-running-number")
@RequiredArgsConstructor
public class AssignRunningNumberController {

    private final AssignRunningNumberService assignRunningNumberService;
    @Autowired
    BatchMasterRepository batchRepo;
    private Long getEventLocationId(HttpSession session) {
        Long id = (Long) session.getAttribute("eventLocationId");
        if (id == null) throw new IllegalStateException("Event location not set. Please log in again.");
        return id;
    }

    private Long getUserId(HttpSession session) {
        Long id = (Long) session.getAttribute("userID");
        if (id == null) throw new IllegalStateException("User not logged in.");
        return id;
    }

    private String getUserName(HttpSession session) {
        String name = (String) session.getAttribute("name");
        return name != null ? name : "User";
    }


    @GetMapping(value = "/page-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AssignPageDataDto> getPageData(HttpSession session) {
        Long eventLocationId = getEventLocationId(session);
        Long userId = getUserId(session);
        String userName = getUserName(session);
        AssignPageDataDto data = assignRunningNumberService.getPageData(eventLocationId, userId, userName);
        return ResponseEntity.ok(data);
    }

    @GetMapping(value = "/current-number", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Long>> getCurrentNumber(HttpSession session) {
        Long eventLocationId = getEventLocationId(session);
        Long userId = getUserId(session);
        List<BatchMaster> lockedByUser = batchRepo.findByEventLocation_IdAndLockedByUserIdAndBatchStatus(eventLocationId, userId, true);
        BatchMaster batch = lockedByUser.get(0);
        long current = assignRunningNumberService.getCurrentRunningNumber(eventLocationId, batch.getBatchId(), batch.getAssignedCount());
        return ResponseEntity.ok(Map.of("currentRunningNumber", current));
    }

    @PostMapping(value = "/lock", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LockBatchResultDto> lockBatch(@RequestBody Map<String, Long> body, HttpSession session) {
        Long batchId = body.get("batchId");
        if (batchId == null) return ResponseEntity.badRequest().body(LockBatchResultDto.builder().success(false).build());
        Long userId = getUserId(session);
        String userName = getUserName(session);
        LockBatchResultDto result = assignRunningNumberService.lockBatch(batchId, userId, userName);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/unlock", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> unlockBatch(@RequestBody Map<String, Long> body, HttpSession session) {
        Long batchId = body.get("batchId");
        if (batchId == null) return ResponseEntity.badRequest().body(Map.of("success", false));
        Long userId = getUserId(session);
        boolean success = assignRunningNumberService.unlockBatch(batchId, userId);
        return ResponseEntity.ok(Map.of("success", success));
    }

    @PostMapping(value = "/create-batch", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AssignBatchDto> createNextBatch(HttpSession session) {
        Long eventLocationId = getEventLocationId(session);
        Long userId = getUserId(session);
        String userName = getUserName(session);
        AssignBatchDto created = assignRunningNumberService.createNextBatch(eventLocationId, userId, userName);
        return ResponseEntity.ok(created);
    }

    @PostMapping(value = "/assign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AssignRunningNumberResultDto> assignRunningNumber(
            @RequestBody Map<String, Object> body,
            HttpSession session) {
        Long batchId = body.get("batchId") != null ? ((Number) body.get("batchId")).longValue() : null;
        Object appNoObj = body.get("applicationNo");
        long applicationNo = appNoObj != null ? ((Number) appNoObj).longValue() : 0L;
        if (batchId == null || applicationNo <= 0) {
            return ResponseEntity.badRequest().body(
                    AssignRunningNumberResultDto.builder().success(false).message("batchId and applicationNo required.").build());
        }
        Long eventLocationId = getEventLocationId(session);
        AssignRunningNumberResultDto result = assignRunningNumberService.assignRunningNumber(batchId, applicationNo, eventLocationId);
        return ResponseEntity.ok(result);
    }
}
