package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShotputDto {
    private Long id;
    private Long applicationNo;

    private Float attempt1;
    private Float attempt2;
    private Float attempt3;

    private Integer marks1;
    private Integer marks2;
    private Integer marks3;

    private Float highestDistance;
    private Integer highestMarks;
}

