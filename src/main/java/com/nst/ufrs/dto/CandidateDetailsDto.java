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
    /** Full name: firstName + fatherName + surname from Candidate. */
    private String name;
    private Integer age;
    private String email;
    private String religion;
    private String post;
    private String gender;
    private LocalDate dob;
    private String applicationCategory;
    private String parallelReservation;
    private Long mobileNo;

    private boolean hasPhoto;
    private boolean hasBiometric1;
    private boolean hasBiometric2;

    /** True if this candidate has already been assigned a running number. */
    private Boolean assignRunningNumberStatus;
    /** Assigned running number when assignRunningNumberStatus is true. */
    private Integer runningNumber;

    /** True if candidate has passed physical test (main or appeal). When true, Appeal 1/2 should not allow fetch. */
    private Boolean physicalTestStatus;

    /** True when photo + biometrics enrollment (attendance) is completed. */
    private Boolean attendance;
}

