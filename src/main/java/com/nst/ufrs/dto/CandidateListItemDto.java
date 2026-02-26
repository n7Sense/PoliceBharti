package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO for listing/searching candidates in the admin UI table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateListItemDto {

    private Long id;
    private Long applicationNo;
    private Long tokenNo;
    private String name;
    private Long mobileNo;
    private String category;
}

