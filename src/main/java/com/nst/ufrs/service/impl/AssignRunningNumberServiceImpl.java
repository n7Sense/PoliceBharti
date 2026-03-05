package com.nst.ufrs.service.impl;

import com.nst.ufrs.domain.BatchMaster;
import com.nst.ufrs.domain.Candidate;
import com.nst.ufrs.domain.EventLocation;
import com.nst.ufrs.domain.GlobalRunningNumber;
import com.nst.ufrs.dto.AssignBatchDto;
import com.nst.ufrs.dto.AssignPageDataDto;
import com.nst.ufrs.dto.AssignRunningNumberResultDto;
import com.nst.ufrs.dto.LockBatchResultDto;
import com.nst.ufrs.repository.BatchMasterRepository;
import com.nst.ufrs.repository.CandidateRepository;
import com.nst.ufrs.repository.EventLocationRepository;
import com.nst.ufrs.repository.GlobalRunningNumberRepository;
import com.nst.ufrs.service.AssignRunningNumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssignRunningNumberServiceImpl implements AssignRunningNumberService {

    private static final int BATCH_SIZE = 20;

    private final BatchMasterRepository batchRepo;
    private final GlobalRunningNumberRepository globalRepo;
    private final EventLocationRepository locationRepo;
    private final CandidateRepository candidateRepo;

    @Override
    @Transactional
    public AssignPageDataDto getPageData(Long eventLocationId, Long userId, String userName) {
        EventLocation location = locationRepo.findById(eventLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Event location not found"));

        // 1. If current user has a locked batch, return only that batch
        List<BatchMaster> lockedByUser = batchRepo.findByEventLocation_IdAndLockedByUserId(eventLocationId, userId);
        if (!lockedByUser.isEmpty()) {
            BatchMaster batch = lockedByUser.get(0);
            long currentRn = getCurrentRunningNumber(eventLocationId);
            return AssignPageDataDto.builder()
                    .batches(List.of(toDto(batch)))
                    .currentRunningNumber(currentRn)
                    .build();
        }

        // 2. Unlocked, active batches (batch_status=true, is_locked=false)
        List<BatchMaster> unlocked = batchRepo.findAllByEventLocation_IdAndBatchStatusAndIsLocked(
                eventLocationId, true, false);

        // 3. If empty, create B1 and ensure GRN is 1
        if (unlocked.isEmpty()) {
            return createFirstBatchAndReturnPageData(location, userId, userName);
        }

        List<AssignBatchDto> dtos = new ArrayList<>();
        for (BatchMaster b : unlocked) {
            dtos.add(toDto(b));
        }
        long currentRn = getCurrentRunningNumber(eventLocationId);
        return AssignPageDataDto.builder()
                .batches(dtos)
                .currentRunningNumber(currentRn)
                .build();
    }

    @Transactional
    protected AssignPageDataDto createFirstBatchAndReturnPageData(EventLocation location, Long userId, String userName) {
        LocalDate today = LocalDate.now();
        BatchMaster b1 = new BatchMaster();
        b1.setEventLocation(location);
        b1.setBatchId(1);
        b1.setBatchCode("B1");
        b1.setBatchName("Batch 1");
        b1.setBatchSize(String.valueOf(BATCH_SIZE));
        b1.setBatchStatus(true);
        b1.setAssignedCount(0);
        b1.setIsLocked(false);
        b1.setCreatedDate(today);
        b1.setCreatedUser(userName);
        b1.setUpdatedDate(today);
        b1 = batchRepo.save(b1);

        // Ensure GlobalRunningNumber exists and next number is 1
        Optional<GlobalRunningNumber> grnOpt = globalRepo.findByEventLocationId(location.getId());
        GlobalRunningNumber grn = grnOpt.orElseGet(() -> {
            GlobalRunningNumber g = new GlobalRunningNumber();
            g.setEventLocation(location);
            g.setCreatedDate(today);
            g.setUpdatedDate(today);
            return g;
        });
        if (grn.getGlobalRunningNumber() == null || grn.getGlobalRunningNumber() < 1) {
            grn.setGlobalRunningNumber(1L);
            grn.setUpdatedDate(today);
            globalRepo.save(grn);
        }

        long currentRn = grn.getGlobalRunningNumber() != null ? grn.getGlobalRunningNumber() : 1L;
        return AssignPageDataDto.builder()
                .batches(List.of(toDto(b1)))
                .currentRunningNumber(currentRn)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public long getCurrentRunningNumber(Long eventLocationId) {
        return globalRepo.findByEventLocationId(eventLocationId)
                .map(g -> g.getGlobalRunningNumber() != null ? g.getGlobalRunningNumber() : 1L)
                .orElse(1L);
    }

    @Override
    @Transactional
    public LockBatchResultDto lockBatch(Long batchId, Long userId, String userName) {
        BatchMaster batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
        if (Boolean.TRUE.equals(batch.getIsLocked())) {
            if (!userId.equals(batch.getLockedByUserId())) {
                return LockBatchResultDto.builder()
                        .success(false)
                        .lockedByUserName(batch.getLockedByUserName() != null ? batch.getLockedByUserName() : "Another user")
                        .batchCode(batch.getBatchCode())
                        .build();
            }
            return LockBatchResultDto.builder().success(true).build();
        }
        batch.setIsLocked(true);
        batch.setLockedByUserId(userId);
        batch.setLockedAt(LocalDateTime.now());
        batch.setLockedByUserName(userName);
        batch.setLastUsedDate(LocalDate.now());
        batchRepo.save(batch);
        return LockBatchResultDto.builder().success(true).build();
    }

    @Override
    @Transactional
    public boolean unlockBatch(Long batchId, Long userId) {
        BatchMaster batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
        if (!userId.equals(batch.getLockedByUserId())) {
            return false;
        }
        batch.setIsLocked(false);
        batch.setLockedByUserId(null);
        batch.setLockedAt(null);
        batch.setLockedByUserName(null);
        batchRepo.save(batch);
        return true;
    }

    @Override
    @Transactional
    public AssignBatchDto createNextBatch(Long eventLocationId, Long userId, String userName) {
        EventLocation location = locationRepo.findById(eventLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Event location not found"));
        LocalDate today = LocalDate.now();
        int nextBatchId = batchRepo.findMaxBatchIdByEventLocation_Id(eventLocationId)
                .map(max -> max + 1)
                .orElse(1);
        BatchMaster batch = new BatchMaster();
        batch.setEventLocation(location);
        batch.setBatchId(nextBatchId);
        batch.setBatchCode("B" + nextBatchId);
        batch.setBatchName("Batch " + nextBatchId);
        batch.setBatchSize(String.valueOf(BATCH_SIZE));
        batch.setBatchStatus(true);
        batch.setAssignedCount(0);
        batch.setIsLocked(false);
        batch.setCreatedDate(today);
        batch.setCreatedUser(userName);
        batch.setUpdatedDate(today);
        batch = batchRepo.save(batch);
        return toDto(batch);
    }

    @Override
    @Transactional
    public AssignRunningNumberResultDto assignRunningNumber(Long batchId, long applicationNo, Long eventLocationId) {
        BatchMaster batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
        if (!batch.getEventLocation().getId().equals(eventLocationId)) {
            return AssignRunningNumberResultDto.builder().success(false).message("Batch does not belong to this event location.").build();
        }
        if (Boolean.FALSE.equals(batch.getIsLocked())) {
            return AssignRunningNumberResultDto.builder().success(false).message("Please lock the batch first.").build();
        }
        if (batch.getAssignedCount() != null && batch.getAssignedCount() >= BATCH_SIZE) {
            return AssignRunningNumberResultDto.builder().success(false).message("Batch is full. Create next batch.").build();
        }

        List<Candidate> candidates = candidateRepo.findAllByApplicationNoOrderByIdDesc(applicationNo);
        if (candidates.isEmpty()) {
            return AssignRunningNumberResultDto.builder().success(false).message("Candidate not found for this Application Number.").build();
        }
        Candidate c = candidates.get(0);
        if (!Boolean.TRUE.equals(c.getAttendance())) {
            return AssignRunningNumberResultDto.builder().success(false).message("Biometric and Physical Test fail – attendance not marked.").build();
        }
        if (!Boolean.TRUE.equals(c.getStatus())) {
            return AssignRunningNumberResultDto.builder().success(false).message("Biometric and Physical Test fail – status not approved.").build();
        }
        if (c.getRunningNumber() != null) {
            return AssignRunningNumberResultDto.builder().success(false).message("Running number already assigned: " + c.getRunningNumber()).build();
        }
        if (c.getBatchMaster() != null && c.getBatchMaster().getId() != null && !c.getBatchMaster().getId().equals(batch.getId())) {
            return AssignRunningNumberResultDto.builder().success(false)
                    .message("Candidate already assigned to another batch.")
                    .build();
        }

        GlobalRunningNumber grn = globalRepo.findByEventLocationIdForUpdate(eventLocationId)
                .orElseThrow(() -> new IllegalStateException("Global running number not found for location"));
        long nextNumber = grn.getGlobalRunningNumber() != null ? grn.getGlobalRunningNumber() : 1L;
        int assigned = (int) nextNumber;

        c.setRunningNumber(assigned);
        c.setBatchMaster(batch);
        candidateRepo.save(c);

        grn.setGlobalRunningNumber(nextNumber + 1);
        grn.setUpdatedDate(LocalDate.now());
        globalRepo.save(grn);

        int newCount = (batch.getAssignedCount() != null ? batch.getAssignedCount() : 0) + 1;
        batch.setAssignedCount(newCount);
        batch.setLastUsedDate(LocalDate.now());
        if (newCount >= BATCH_SIZE) {
            batch.setBatchStatus(false);
        }
        batch.setUpdatedDate(LocalDate.now());
        batchRepo.save(batch);

        return AssignRunningNumberResultDto.builder()
                .success(true)
                .message("Running number " + assigned + " assigned successfully.")
                .assignedRunningNumber(assigned)
                .build();
    }

    private static AssignBatchDto toDto(BatchMaster b) {
        int size = 20;
        try {
            if (b.getBatchSize() != null) size = Integer.parseInt(b.getBatchSize().trim());
        } catch (Exception ignored) {}
        return AssignBatchDto.builder()
                .id(b.getId())
                .batchId(b.getBatchId())
                .batchCode(b.getBatchCode())
                .batchName(b.getBatchName())
                .assignedCount(b.getAssignedCount() != null ? b.getAssignedCount() : 0)
                .batchSize(size)
                .batchStatus(b.getBatchStatus())
                .isLocked(b.getIsLocked())
                .lockedByUserId(b.getLockedByUserId())
                .lockedByUserName(b.getLockedByUserName())
                .build();
    }
}
