package com.nst.ufrs.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a Police Bharti Candidate.
 * Mapped to the `candidate` table in MySQL.
 */
@Entity
@Table(name = "candidate", indexes = {
        @Index(name = "idx_token_no", columnList = "token_no"),
        @Index(name = "idx_application_no", columnList = "application_no"),
        @Index(name = "idx_mobile_no", columnList = "mobile_no")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"id"})
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sr_no")
    private Integer srNo;

    @Column(name = "username", length = 150)
    private String username;

    /** Primary key from Excel - must be numeric (Long) */
    @Column(name = "token_no",unique = true)
    private Long tokenNo;

    /** Must be numeric (Long) */
    @Column(name = "application_no", unique = true)
    private Long applicationNo;

    @Column(name = "exam_fee")
    private Double examFee;

    @Column(name = "post", length = 100)
    private String post;

    @Column(name = "unit_name", length = 150)
    private String unitName;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "father_name", length = 100)
    private String fatherName;

    @Column(name = "surname", length = 100)
    private String surname;

    @Column(name = "mother_name", length = 100)
    private String motherName;

    @Column(name = "gender", length = 100)
    private String gender;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "email_id", length = 150)
    private String emailId;

    @Column(name = "mobile_no")
    private Long mobileNo;

    @Column(name = "religion", length = 50)
    private String religion;

    @Column(name = "caste", length = 100)
    private String caste;

    @Column(name = "sub_caste", length = 100)
    private String subCaste;

    @Column(name = "application_category", length = 50)
    private String applicationCategory;

    @Column(name = "parallel_reservation", length = 100)
    private String parallelReservation;

    @Column(name = "non_cremelayer", length = 5)
    private String nonCremelayer;

    @Column(name = "maharashtra_domicile", length = 5)
    private String maharashtraDomicile;

    @Column(name = "maharashtra_domicile_cert_no", length = 100)
    private String maharashtraDomicileCertNo;

    @Column(name = "maharashtra_domicile_date")
    private LocalDate maharashtraDomicileDate;

    @Column(name = "karnataka_domicile", length = 5)
    private String karnatakaDomicile;

    @Column(name = "karnataka_domicile_cert_no", length = 100)
    private String karnatakaDomicileCertNo;

    @Column(name = "karnataka_domicile_date")
    private LocalDate karnatakaDomicileDate;

    @Column(name = "ex_soldier", length = 5)
    private String exSoldier;

    @Column(name = "home_guard", length = 5)
    private String homeGuard;

    @Column(name = "prakalpgrast", length = 5)
    private String prakalpgrast;

    @Column(name = "bhukampgrast", length = 5)
    private String bhukampgrast;

    @Column(name = "sportsperson", length = 5)
    private String sportsperson;

    @Column(name = "parttime", length = 5)
    private String parttime;

    @Column(name = "female_reservation", length = 5)
    private String femaleReservation;

    @Column(name = "parent_in_police", length = 5)
    private String parentInPolice;

    @Column(name = "police_rank", length = 100)
    private String policeRank;

    @Column(name = "police_nature_of_employment", length = 100)
    private String policeNatureOfEmployment;

    @Column(name = "police_details", length = 255)
    private String policeDetails;

    @Column(name = "anath", length = 5)
    private String anath;

    @Column(name = "anath_date")
    private LocalDate anathDate;

    @Column(name = "anath_certificate_type", length = 100)
    private String anathCertificateType;

    @Column(name = "ex_service_dependent", length = 5)
    private String exServiceDependent;

    @Column(name = "is_ncc", length = 5)
    private String isNcc;

    @Column(name = "ncc_certificate_no", length = 100)
    private String nccCertificateNo;

    @Column(name = "ncc_date")
    private LocalDate nccDate;

    @Column(name = "naxalite_area", length = 5)
    private String naxaliteArea;

    @Column(name = "small_vehicle", length = 5)
    private String smallVehicle;

    @Column(name = "ex_service_joining_date")
    private LocalDate exServiceJoiningDate;

    @Column(name = "caste_certificate_no", length = 100)
    private String casteCertificateNo;

    @Column(name = "caste_certificate_date")
    private LocalDate casteCertificateDate;

    @Column(name = "work_on_contract", length = 5)
    private String workOnContract;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "place", length = 100)
    private String place;

    // SSC Education
    @Column(name = "ssc_board_name", length = 150)
    private String sscBoardName;

    @Column(name = "ssc_result", length = 20)
    private String sscResult;

    @Column(name = "ssc_marks_obtained")
    private Integer sscMarksObtained;

    @Column(name = "ssc_total_marks")
    private Integer sscTotalMarks;

    // HSC Education
    @Column(name = "hsc_board_name")
    private String hscBoardName;

    @Column(name = "hsc_result", length = 20)
    private String hscResult;

    @Column(name = "hsc_marks_obtained")
    private Integer hscMarksObtained;

    @Column(name = "hsc_total_marks")
    private Integer hscTotalMarks;

    // Seventh Education
    @Column(name = "seventh_board_name")
    private String seventhBoardName;

    @Column(name = "seventh_result", length = 20)
    private String seventhResult;

    @Column(name = "seventh_marks_obtained")
    private Integer seventhMarksObtained;

    @Column(name = "seventh_total_marks")
    private Integer seventhTotalMarks;

    // Diploma Education
    @Column(name = "diploma_board_name")
    private String diplomaBoardName;

    @Column(name = "diploma_result", length = 20)
    private String diplomaResult;

    @Column(name = "diploma_marks_obtained")
    private Integer diplomaMarksObtained;

    @Column(name = "diploma_total_marks")
    private Integer diplomaTotalMarks;

    @Column(name = "mscit", length = 5)
    private String mscit;

    @Column(name = "graduation_degree", length = 100)
    private String graduationDegree;

    @Column(name = "post_graduation_degree", length = 100)
    private String postGraduationDegree;

    @Column(name = "other_graduation_degree", length = 100)
    private String otherGraduationDegree;

    @Column(name = "other_post_graduation_degree", length = 100)
    private String otherPostGraduationDegree;

    @Column(name = "is_farmer_suicide", length = 5)
    private String isFarmerSuicide;

    @Column(name = "farmer_suicide_report_no", length = 100)
    private String farmerSuicideReportNo;

    @Column(name = "farmer_suicide_report_date")
    private LocalDate farmerSuicideReportDate;

    // Audit fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "Aadhar")
    private Long aadhaarNumber;

    /**
     * Candidate passport photo captured from webcam.
     * Stored as Base64 (data URL or raw base64) in LONGTEXT.
     */
    @Lob
    @Column(name = "photo", columnDefinition = "LONGTEXT")
    private String photo;

    /**
     * Left thumb biometric template (Aratek A600).
     */
    @Lob
    @Column(name = "biometric1", columnDefinition = "LONGTEXT")
    private String biometric1;

    /**
     * Right thumb biometric template (Aratek A600).
     */
    @Lob
    @Column(name = "biometric2", columnDefinition = "LONGTEXT")
    private String biometric2;

    @Column(name = "attendance")
    private Boolean attendance = false;

    @Column(name = "status")
    private Boolean status = false;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}