package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCandidateRowDto {
    private Long id;
    private Integer srNo;
    private Integer runningNumber;
    private Long applicationNo;
    private Long tokenNo;
    private String name;
    private Long mobileNo;
    private String post;
    private String category;
}

