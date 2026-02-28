package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalTestUpsertRequest {
    private Long applicationNo;
    private Float height;
    private Float chest;
    private Float expandedChest;

    // optional manual reason override (else auto-generated)
    private String rejectReason;

    // Appeal 1
    private Float height1;
    private Float chest1;
    private Float expandedChest1;
    private String rejectReason1;

    // Appeal 2
    private Float height2;
    private Float chest2;
    private Float expandedChest2;
    private String rejectReason2;
}

