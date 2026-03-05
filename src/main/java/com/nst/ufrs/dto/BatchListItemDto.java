package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchListItemDto {
    private Long id;
    private Integer batchId;
    private String batchCode;
    private String batchName;
    private Integer batchSize;
    private Integer assignedCount;
    private Boolean batchStatus;
    private Boolean isLocked;
    private LocalDate lastUsedDate;
}

