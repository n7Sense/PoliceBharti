package com.nst.ufrs.repository;

import com.nst.ufrs.domain.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
