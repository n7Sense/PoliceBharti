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
}
