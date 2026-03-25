package com.nst.ufrs.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private Integer name;

    /**
     *  This Column represent the Unit for Event if Event in 100 then unit m - meter
     *  m - meter
     *  km - kilo meter
     */
    @Column(name = "unit")
    private String unit;

    @Column(name = "description")
    private String description;

    @Column(name = "lap")
    private Integer lap = 0;

    @Column(name = "rfid_number")
    private Integer rfidNumber;

    /**
     *  runningNumber is also called as chestNumber
     */
    @Column(name = "running_number")
    private Integer runningNumber;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "time_difference")
    private Double timeDifference;

    @Column(name = "marks")
    private Double marks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "event_location_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_event_event_location")
    )
    @JsonIgnore
    private EventLocation eventLocation;

    // Audit fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
