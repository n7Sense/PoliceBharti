package com.nst.ufrs.repository;

import com.nst.ufrs.domain.PhysicalTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhysicalTestRepository extends JpaRepository<PhysicalTest, Long> {

    Optional<PhysicalTest> findByCandidateId(Long candidateId);

    @Query("""
            SELECT pt
            FROM PhysicalTest pt
            JOIN pt.candidate c
            WHERE c.applicationNo = :applicationNo
            ORDER BY pt.id DESC
            """)
    List<PhysicalTest> findAllByApplicationNoOrderByIdDesc(@Param("applicationNo") Long applicationNo);
}

