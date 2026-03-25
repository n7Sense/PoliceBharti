package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload sent from Document Verification page on submit.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVerificationDecisionRequest {

    private Long applicationNo;

    /**
     * true  -> all required certificates verified, candidate accepted
     * false -> one or more required certificates missing, candidate rejected
     */
    private boolean allRequiredVerified;
}

