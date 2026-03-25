package com.nst.ufrs.repository;

import com.nst.ufrs.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByEventLocationIdOrderByIdDesc(Long eventLocationId, Pageable pageable);

    List<Event> findAllByEventLocationIdAndRunningNumberOrderByIdDesc(Long eventLocationId, Integer runningNumber);

    List<Event> findAllByEventLocationIdAndNameAndUnitAndRunningNumberIn(
            Long eventLocationId,
            Integer name,
            String unit,
            java.util.Collection<Integer> runningNumbers
    );
}

