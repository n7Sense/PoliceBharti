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
}

