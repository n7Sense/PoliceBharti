package com.nst.ufrs.repository;

import com.nst.ufrs.domain.BatchMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchMasterRepository extends JpaRepository<BatchMaster, Long> {

    Optional<BatchMaster> findTopByEventLocationIdOrderByIdDesc(Long eventLocationId);

    Optional<BatchMaster> findById(Long batchMasterId);

    //List<BatchMaster> findUnlockedActiveBatches(Long eventLocationId);

    List<BatchMaster> findAllByBatchStatusAndIsLocked(Long eventLocationId, Boolean batchStatus, Boolean isLocked);
}
