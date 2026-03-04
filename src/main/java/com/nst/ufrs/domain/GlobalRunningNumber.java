package com.nst.ufrs.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "global_running_number",
        uniqueConstraints = @UniqueConstraint(columnNames = "event_location_id"))
public class GlobalRunningNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "global_running_number")
    private Long globalRunningNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_location_id", nullable = false)
    private EventLocation eventLocation;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;
}