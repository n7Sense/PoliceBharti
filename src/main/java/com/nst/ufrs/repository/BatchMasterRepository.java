package com.nst.ufrs.repository;

import com.nst.ufrs.domain.BatchMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchMasterRepository extends JpaRepository<BatchMaster, Long> {

    Optional<BatchMaster> findTopByEventLocation_IdOrderByIdDesc(Long eventLocationId);

    Optional<BatchMaster> findById(Long batchMasterId);

    List<BatchMaster> findAllByEventLocation_Id(Long eventLocationId);

    List<BatchMaster> findAllByEventLocation_IdAndBatchStatusAndIsLocked(Long eventLocationId, Boolean batchStatus, Boolean isLocked);

    List<BatchMaster> findByEventLocation_IdAndLockedByUserIdAndBatchStatus(Long eventLocationId, Long lockedByUserId, Boolean batchStatus);


    //AND b.batchStatus = :batchStatus
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

    @Query("SELECT MAX(b.todayBatchId) FROM BatchMaster b WHERE b.eventLocation.id = :eventLocationId AND b.createdDate = :createdDate")
    Optional<Integer> findMaxTodayBatchIdByEventLocation_Id(@Param("eventLocationId") Long eventLocationId, @Param("createdDate")LocalDate createdDate);

    /**
     * Delete old batches (not created today) that have no assignments (assignedCount = 0 or null).
     * Call before updateOldBatchesSetInactiveAndUnlocked.
     */
    @Modifying
    @Query("""
            DELETE FROM BatchMaster b
            WHERE b.eventLocation.id = :eventLocationId
              AND b.createdDate <> :today
              AND (b.assignedCount = 0 OR b.assignedCount IS NULL)
            """)
    int deleteOldEmptyBatches(
            @Param("eventLocationId") Long eventLocationId,
            @Param("today") LocalDate today);

    /**
     * For a given event location, set batchStatus = false and isLocked = false (and clear lock fields)
     * for all batches created on a date other than today. Call before initializeRunningNumber.
     */
    @Modifying
    @Query("""
            UPDATE BatchMaster b SET
                b.batchStatus = false,
                b.isLocked = false,
                b.lockedByUserId = null,
                b.lockedAt = null,
                b.lockedByUserName = null
            WHERE b.eventLocation.id = :eventLocationId
              AND b.createdDate <> :today
            """)
    int updateOldBatchesSetInactiveAndUnlocked(
            @Param("eventLocationId") Long eventLocationId,
            @Param("today") LocalDate today);
}
