package com.nst.ufrs.service.impl;

import com.nst.ufrs.domain.BatchMaster;
import com.nst.ufrs.domain.Candidate;
import com.nst.ufrs.domain.EventLocation;
import com.nst.ufrs.domain.GlobalRunningNumber;
import com.nst.ufrs.repository.BatchMasterRepository;
import com.nst.ufrs.repository.CandidateRepository;
import com.nst.ufrs.repository.EventLocationRepository;
import com.nst.ufrs.repository.GlobalRunningNumberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RunningNumberServiceImpl {

    @Autowired
    private BatchMasterRepository batchRepo;

    @Autowired
    private GlobalRunningNumberRepository globalRepo;

    @Autowired
    private EventLocationRepository locationRepo;

    @Autowired
    private CandidateRepository candidateRepository;

    public void initializeRunningNumber(Long eventLocationId) {

        EventLocation location = locationRepo.findById(eventLocationId).orElseThrow(() -> new RuntimeException("Event Location not found"));

        LocalDate today = LocalDate.now();

        // Latest batch for this location
        Optional<BatchMaster> latestBatch = batchRepo.findTopByEventLocationIdOrderByIdDesc(eventLocationId);

        Optional<GlobalRunningNumber> existingGlobal = globalRepo.findByEventLocationId(eventLocationId);

        // Case 1: No BatchMaster → insert new GRN if not exists
        if (latestBatch.isEmpty()) {
            if (existingGlobal.isEmpty()) {
                GlobalRunningNumber grn = new GlobalRunningNumber();
                grn.setEventLocation(location);
                grn.setGlobalRunningNumber(0L);
                grn.setCreatedDate(today);
                globalRepo.save(grn);
            }
            return;
        }

        // Case 2: BatchMaster exists → update only if last batch is previous date
        BatchMaster batch = latestBatch.get();
        if (!batch.getCreatedDate().isEqual(today)) {

            GlobalRunningNumber grn = existingGlobal .orElse(new GlobalRunningNumber());
            grn.setEventLocation(location);
            // Next running number = batch start + assignedCount
            // Example: start=81, assigned=15 => next=96
            int batchSize = parseBatchSize(batch.getBatchSize());
            int assigned = batch.getAssignedCount() != null ? batch.getAssignedCount() : 0;
            long next = (batch.getBatchId() != null ? batch.getBatchId().longValue() : 0L) + Math.min(assigned, batchSize);
            grn.setGlobalRunningNumber(next);
            grn.setUpdatedDate(today);

            globalRepo.save(grn);
        }
        // else: batch today → do nothing
    }



    private int parseBatchSize(String batchSize) {
        try {
            return Integer.parseInt(batchSize == null ? "20" : batchSize.trim());
        } catch (Exception e) {
            return 20;
        }
    }

    private int parseBatchNo(String batchCode) {
        if (batchCode == null) return 0;
        String c = batchCode.trim().toUpperCase();
        if (c.startsWith("B")) {
            try { return Integer.parseInt(c.substring(1)); } catch (Exception ignored) {}
        }
        return 0;
    }
}