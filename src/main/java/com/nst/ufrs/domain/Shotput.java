package com.nst.ufrs.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "shotput",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_shotput_candidate_id", columnNames = {"candidate_id"})
        },
        indexes = {
                @Index(name = "idx_shotput_candidate_id", columnList = "candidate_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shotput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_shotput_candidate"))
    private Candidate candidate;

    @Column(name = "attempt1")
    private Float attempt1;

    @Column(name = "attempt2")
    private Float attempt2;

    @Column(name = "attempt3")
    private Float attempt3;

    @Column(name = "marks1")
    private Integer marks1;

    @Column(name = "marks2")
    private Integer marks2;

    @Column(name = "marks3")
    private Integer marks3;

    @Column(name = "highest_distance")
    private Float highestDistance;

    @Column(name = "highest_marks")
    private Integer highestMarks;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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

