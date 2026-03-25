package com.nst.ufrs.repository;

import com.nst.ufrs.domain.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Candidate entity.
 * Spring Data JPA provides CRUD + bulk save out of the box.
 */
@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByTokenNo(Long tokenNo);

    Optional<Candidate> findByApplicationNo(Long applicationNo);

    List<Candidate> findAllByApplicationNoOrderByIdDesc(Long applicationNo);

    List<Candidate> findAllByAttendance(Boolean attendance);
    List<Candidate> findAllByStatus(Boolean attendance);
    List<Candidate> findAllByPhysicalTestStatus(Boolean attendance);

    boolean existsByTokenNo(Long tokenNo);

    boolean existsByApplicationNo(Long applicationNo);

    List<Candidate> findByGender(String gender);

    List<Candidate> findByPost(String post);

    /**
     * Recent candidates ordered by creation time (or id if createdAt is null).
     */
    @Query("SELECT c FROM Candidate c ORDER BY c.createdAt DESC, c.id DESC")
    List<Candidate> findRecentCandidatesLimited(org.springframework.data.domain.Pageable pageable);

    /**
     * Search by full name (firstName + space + surname) using a case-insensitive LIKE.
     */
    @Query("""
            SELECT c FROM Candidate c
            WHERE LOWER(CONCAT(COALESCE(c.firstName, ''), ' ', COALESCE(c.surname, '')))
                  LIKE LOWER(CONCAT('%', :name, '%'))
            """)
    List<Candidate> searchByFullName(@Param("name") String name);

    @Query("SELECT COUNT(c) FROM Candidate c WHERE c.tokenNo IS NOT NULL")
    long countValidCandidates();

    @Query("SELECT c.applicationNo FROM Candidate c WHERE c.applicationNo IN :appNos")
    List<Long> findExistingApplicationNos(@Param("appNos") Collection<Long> appNos);

    @Query("SELECT c.tokenNo FROM Candidate c WHERE c.tokenNo IN :tokenNos")
    List<Long> findExistingTokenNos(@Param("tokenNos") Collection<Long> tokenNos);

    @Query("""
            SELECT c FROM Candidate c
            WHERE c.batchMaster.id = :batchId
              AND c.batchMaster.eventLocation.id = :eventLocationId
            ORDER BY
              CASE WHEN c.runningNumber IS NULL THEN 1 ELSE 0 END,
              c.runningNumber ASC,
              c.id ASC
            """)
    List<Candidate> findAllByBatchForEventLocation(
            @Param("batchId") Long batchId,
            @Param("eventLocationId") Long eventLocationId
    );

    @Query("SELECT MAX(c.runningNumber) FROM Candidate c")
    Optional<Integer> findMaxRunningNumber();

    @Query("""
            SELECT c FROM Candidate c
            WHERE (:fromDate IS NULL OR c.applicationDate >= :fromDate)
              AND (:toDate IS NULL OR c.applicationDate <= :toDate)
              AND (
                    :statusType IS NULL
                 OR (:statusType = 'ATTENDANCE' AND c.attendance = :approved)
                 OR (:statusType = 'DOCUMENTS' AND c.documentStatus = :approved)
                 OR (:statusType = 'PHYSICAL_TEST' AND c.physicalTestStatus = :approved)
                 OR (:statusType = 'RUNNING_NO' AND c.assignRunningNumberStatus = :approved)
                 OR (:statusType = 'RESULT_STATUS' AND c.resultStatus IS NOT NULL AND c.resultStatus = :approved)
              )
            ORDER BY c.applicationNo ASC
            """)
    org.springframework.data.domain.Page<Candidate> searchForCandidateMaster(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("statusType") String statusType,
            @Param("approved") Boolean approved,
            org.springframework.data.domain.Pageable pageable
    );
}
