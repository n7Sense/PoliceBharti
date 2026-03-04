package com.nst.ufrs.repository;

import com.nst.ufrs.domain.GlobalRunningNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlobalRunningNumberRepository extends JpaRepository<GlobalRunningNumber, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM GlobalRunningNumber g WHERE g.eventLocation.id = :locationId")
    Optional<GlobalRunningNumber> findByEventLocationIdForUpdate(Long locationId);

    Optional<GlobalRunningNumber> findByEventLocationId(Long locationId);
}
