package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunningNumberBatchDto {
    private Long id;
    private Integer batchId; // start running number for this batch
    private String batchCode;
    private String batchName;
    private int batchSize;
    private boolean batchStatus;
    private boolean locked;
    private int assignedCount;
    private Integer currentRunningNumber;
    private Integer endRunningNumber;
    private String lockedByUsername;
    private LocalDate createdDate;
    private LocalDate lastUsedDate;
    private LocalDateTime lockedAt;
}

