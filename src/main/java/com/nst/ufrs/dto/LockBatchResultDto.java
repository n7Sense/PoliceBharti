package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockBatchResultDto {

    private boolean success;
    /** When success is false: name of user who already locked the batch */
    private String lockedByUserName;
    /** When success is false: batch code (e.g. B1) for message */
    private String batchCode;
}
