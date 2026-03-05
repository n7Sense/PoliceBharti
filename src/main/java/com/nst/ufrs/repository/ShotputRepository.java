package com.nst.ufrs.repository;

import com.nst.ufrs.domain.Shotput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShotputRepository extends JpaRepository<Shotput, Long> {

    Optional<Shotput> findByCandidateId(Long candidateId);

    @Query("""
            SELECT s
            FROM Shotput s
            JOIN s.candidate c
            WHERE c.applicationNo = :applicationNo
            ORDER BY s.id DESC
            """)
    List<Shotput> findAllByApplicationNoOrderByIdDesc(@Param("applicationNo") Long applicationNo);
}

