package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Candidate details used by Add Candidate screen.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDetailsDto {
    private Long applicationNo;
    private String post;
    private String gender;
    private LocalDate dob;
    private String applicationCategory;
    private String parallelReservation;
    private Long mobileNo;

    private boolean hasPhoto;
    private boolean hasBiometric1;
    private boolean hasBiometric2;
}

