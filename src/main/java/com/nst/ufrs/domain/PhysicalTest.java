package com.nst.ufrs.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "physical_test",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_physical_test_candidate_id", columnNames = {"candidate_id"})
        },
        indexes = {
                @Index(name = "idx_physical_test_candidate_id", columnList = "candidate_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_physical_test_candidate"))
    private Candidate candidate;

    @Column(name = "height")
    private Float height;

    @Column(name = "chest")
    private Float chest;

    @Column(name = "expanded_chest")
    private Float expandedChest;

    @Column(name = "status")
    private Boolean status;

    /**
     *  If Candidate Reject in Physical Test
     * Required
     * @height >= 5.5 fit
     * @chest >= 30 inch so if he have any dout then he will go for Appeal 1
     * again physical test
     */
    @Column(name = "height1")
    private Float height1;

    @Column(name = "chest1")
    private Float chest1;

    @Column(name = "expanded_chest1")
    private Float expandedChest1;

    @Column(name = "status1")
    private Boolean status1;

    /**
     * iF Candidate rejected in Appeal 1 So he will go for Appeal 2
     */
    @Column(name = "height2")
    private Float height2;

    @Column(name = "chest2")
    private Float chest2;

    @Column(name = "expanded_chest2")
    private Float expandedChest2;

    @Column(name = "status2")
    private Boolean status2;

}
