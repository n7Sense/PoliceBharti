package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalTestDto {
    private Long id;
    private Long applicationNo;
    private Float height;
    private Float chest;
    private Float expandedChest;
    private Boolean status;
    private String rejectReason;

    private Float height1;
    private Float chest1;
    private Float expandedChest1;
    private Boolean status1;
    private String rejectReason1;

    private Float height2;
    private Float chest2;
    private Float expandedChest2;
    private Boolean status2;
    private String rejectReason2;
}

