package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for saving candidate photo + biometrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateEnrollmentRequest {
    private Long applicationNo;
    private String photo;       // Base64 data URL (preferred) or raw base64
    private String biometric1;  // left thumb template
    private String biometric2;  // right thumb template
}

