package com.nst.ufrs.repository;

import com.nst.ufrs.domain.EventLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLocationRepository extends JpaRepository<EventLocation, Long> {
}