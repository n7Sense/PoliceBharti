package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sensitive payload used only for on-site verification (physical test).
 * Contains stored photo + biometric templates for matching.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateVerificationDataDto {
    private Long applicationNo;
    private String photo;      // base64 data URL or raw base64 (as stored)
    private String biometric1; // stored left thumb template/feature
    private String biometric2; // stored right thumb template/feature
}

