package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignPageDataDto {

    /** Batches for dropdown: either [batch locked by current user] or all unlocked batches */
    private List<AssignBatchDto> batches;
    /** Next running number to assign (from GlobalRunningNumber) */
    private long currentRunningNumber;
}
