package com.nst.ufrs.service;

import com.nst.ufrs.dto.AssignBatchDto;
import com.nst.ufrs.dto.AssignPageDataDto;
import com.nst.ufrs.dto.AssignRunningNumberResultDto;
import com.nst.ufrs.dto.LockBatchResultDto;

import java.util.List;

public interface AssignRunningNumberService {

    /**
     * Data for assign-running-number page: batches for dropdown and current running number.
     * If current user has a locked batch, returns only that batch. Else returns unlocked batches;
     * if none exist, creates B1 and returns it.
     */
    AssignPageDataDto getPageData(Long eventLocationId, Long userId, String userName);

    /**
     * Current (next) running number for the event location from GlobalRunningNumber.
     */
    long getCurrentRunningNumber(Long eventLocationId);

    /**
     * Lock the batch for the current user. If already locked by another user, returns success=false with lockedByUserName.
     */
    LockBatchResultDto lockBatch(Long batchId, Long userId, String userName);

    /**
     * Unlock the batch only if it is locked by the given user.
     */
    boolean unlockBatch(Long batchId, Long userId);

    /**
     * Create next batch (B2, B3, ...) for the event location.
     */
    AssignBatchDto createNextBatch(Long eventLocationId, Long userId, String userName);

    /**
     * Assign running number to candidate: validate attendance & status, set candidate.runningNumber, increment batch and GRN.
     */
    AssignRunningNumberResultDto assignRunningNumber(Long batchId, long applicationNo, Long eventLocationId);
}
