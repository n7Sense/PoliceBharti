package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateMasterRowDto {

    private Integer srNo;
    private Long tokenNo;
    private Long applicationNo;
    private String post;
    private String firstName;
    private String fatherName;
    private String surname;
    private String motherName;
    private LocalDate dob;
    private Integer age;
    private String gender;
    private String category;
    private String reservationType;
    private Long mobileNo;

    // Calculated / status
    private Boolean resultStatus;

    // 100m running
    private LocalDateTime hundredStartTime;
    private LocalDateTime hundredEndTime;
    private Double hundredTimeDiff;
    private Double hundredMarks;

    // 5km running
    private LocalDateTime fiveKmStartTime;
    private LocalDateTime fiveKmEndTime;
    private Double fiveKmTimeDiff;
    private Double fiveKmMarks;

    // Shotput
    private Float shotputAttempt1;
    private Float shotputAttempt2;
    private Float shotputAttempt3;
    private Float shotputHighestDistance;
    private Integer shotputMarks;

    private Double totalMarks;
}

