package com.nst.ufrs.repository;

import com.nst.ufrs.domain.BatchMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchMasterRepository extends JpaRepository<BatchMaster, Long> {

    Optional<BatchMaster> findTopByEventLocation_IdOrderByIdDesc(Long eventLocationId);

    Optional<BatchMaster> findById(Long batchMasterId);

    List<BatchMaster> findAllByEventLocation_IdAndBatchStatusAndIsLocked(Long eventLocationId, Boolean batchStatus, Boolean isLocked);

    /** Batch(es) locked by the given user at this event location (at most one in practice). */
    List<BatchMaster> findByEventLocation_IdAndLockedByUserId(Long eventLocationId, Long userId);

    @Query("""
            SELECT b FROM BatchMaster b
            WHERE b.id = :batchId
              AND b.eventLocation.id = :eventLocationId
            """)
    Optional<BatchMaster> findByIdAndEventLocationId(
            @Param("batchId") Long batchId,
            @Param("eventLocationId") Long eventLocationId
    );

    @Query("""
            SELECT b FROM BatchMaster b
            WHERE b.eventLocation.id = :eventLocationId
            ORDER BY b.batchId ASC, b.id ASC
            """)
    List<BatchMaster> findAllForEventLocation(@Param("eventLocationId") Long eventLocationId);

    @Query("SELECT MAX(b.batchId) FROM BatchMaster b WHERE b.eventLocation.id = :eventLocationId")
    Optional<Integer> findMaxBatchIdByEventLocation_Id(@Param("eventLocationId") Long eventLocationId);
}
