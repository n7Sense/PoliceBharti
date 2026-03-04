package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignBatchDto {
    private Long id;
    private Integer batchId;
    private String batchCode;
    private String batchName;
    private Integer assignedCount;
    private Integer batchSize;
    private Boolean batchStatus;
    private Boolean isLocked;
    private Long lockedByUserId;
    private String lockedByUserName;
}
