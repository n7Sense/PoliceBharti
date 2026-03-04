package com.nst.ufrs.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "batch_master",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_location_id", "batch_id"}))
public class BatchMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id")
    private Integer batchId;

    @Column(name = "batch_name")
    private String batchName;

    @Column(name = "batch_code")
    private String batchCode;

    @Column(name = "description")
    private String description;

    @Column(name = "batch_size")
    private String batchSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_location_id", nullable = false)
    private EventLocation eventLocation;

    @Column(name = "created_user")
    private String createdUser;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    /**
     * this column is created to ensure the 20 candidate assign to 1 batch
     * by defaults its value will be true after 20 candidate assign it will update to false
     * to ensure its full
     */
    @Column(name = "batch_status")
    private Boolean batchStatus = true;

    /**
     * Lock status - true when batch is locked by a user, false when unlocked
     * Default value is false (unlocked)
     */
    @Column(name = "is_locked")
    private Boolean isLocked = false;

    /**
     * Number of candidates assigned to this batch (0-20)
     * Increments with each chesnt number assignment
     * When reaches 20, batchStatus is set to 1 (full)
     */
    @Column(name = "assigned_count")
    private Integer assignedCount = 0;


    /**
     * User ID of the user who locked this batch
     * Used for tracking and faster queries
     */
    @Column(name = "locked_by_user_id")
    private Long lockedByUserId;

    /**
     * Timestamp when the batch was locked
     * Used for tracking and potential auto-unlock after timeout
     */
    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    /**
     * Username for display purposes (denormalized for convenience)
     */
    @Column(name = "locked_by_username")
    private String lockedByUserName;

    /**
     * Last date when this batch was used for assignment
     * Helps track batch activity and daily completion
     */
    @Column(name = "last_used_date")
    private LocalDate lastUsedDate;
}