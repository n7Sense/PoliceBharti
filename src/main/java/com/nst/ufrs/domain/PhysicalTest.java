package com.nst.ufrs.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Physical_Test")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "height")
    private Float height;

    @Column(name = "chest")
    private Float chest;

    @Column(name = "expanded_chest")
    private Float expandedChest;

    @Column(name = "status")
    private Boolean status;

    // For Appeal 1
    @Column(name = "height1")
    private Float height1;

    @Column(name = "chest1")
    private Float chest1;

    @Column(name = "expanded_chest1")
    private Float expandedChest1;

    @Column(name = "status1")
    private Boolean status1;

    // For Appeal 2
    @Column(name = "height2")
    private Float height2;

    @Column(name = "chest2")
    private Float chest2;

    @Column(name = "expanded_chest2")
    private Float expandedChest2;

    @Column(name = "status2")
    private Boolean status2;

}
