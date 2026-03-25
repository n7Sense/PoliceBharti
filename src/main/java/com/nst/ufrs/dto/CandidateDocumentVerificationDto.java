package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for document verification screen.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDocumentVerificationDto {

    private Long applicationNo;
    private String name;

    private String gender;
    private Long mobileNo;
    private java.time.LocalDate dob;
    private Integer age;
    private String email;
    private String post;
    private String religion;
    private String applicationCategory;
    private String parallelReservation;

    /** Passport-size photo (data URL or raw base64). */
    private String photo;

    /** Special rule flag – when true SSC/HSC not required and 7th is mandatory instead. */
    private Boolean naxaliteArea;

    /** Overall document verification status flag stored on Candidate. */
    private Boolean documentStatus;

    // Certificate / reservation related flags copied from Candidate
    private Boolean nonCremelayer;
    private Boolean maharashtraDomicile;
    private Boolean karnatakaDomicile;
    private Boolean exSoldier;
    private Boolean homeGuard;
    private Boolean prakalpgrast;
    private Boolean bhukampgrast;
    private Boolean sportsperson;
    private Boolean femaleReservation;
    private Boolean parentInPolice;
    private Boolean anath;
    private Boolean exServiceDependent;
    private Boolean isNcc;
    private Boolean smallVehicle;
    private Boolean workOnContract;
    private Boolean mscit;
    private Boolean isFarmerSuicide;
}

