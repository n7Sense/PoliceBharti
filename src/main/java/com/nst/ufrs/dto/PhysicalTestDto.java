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
}

