package com.nst.ufrs.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;


@Data
@Entity
@Table(name = "event_location")
public class EventLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_location")
    private String event_location;

    @Column(name = "created_date")
    private Date created_date;

    @Column(name = "modified_date")
    private Date modified_date;

    @Column(name = "created_user")
    private String created_user;

    @Column(name = "modified_user")
    private String modified_user;

}
