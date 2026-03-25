package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignRunningNumberResultDto {

    private boolean success;
    private String message;
    /** Assigned running number when success */
    private Integer assignedRunningNumber;
    /** True when this assignment made the batch full (20 candidates). */
    private Boolean batchFull;
    /** Batch code that just became full (e.g. "B1"). */
    private String fullBatchCode;
    /** Suggested next batch code (e.g. "B2") when batchFull is true. */
    private String nextBatchCode;
}
