package com.nst.ufrs.service;

import com.nst.ufrs.domain.Candidate;
import com.nst.ufrs.domain.PhysicalTest;
import com.nst.ufrs.dto.PhysicalTestDto;
import com.nst.ufrs.dto.PhysicalTestUpsertRequest;
import com.nst.ufrs.repository.CandidateRepository;
import com.nst.ufrs.repository.PhysicalTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhysicalTestService {

    private final CandidateRepository candidateRepository;
    private final PhysicalTestRepository physicalTestRepository;

    @Transactional(readOnly = true)
    public PhysicalTestDto getByApplicationNo(long applicationNo) {
        List<PhysicalTest> matches = physicalTestRepository.findAllByApplicationNoOrderByIdDesc(applicationNo);
        if (matches.isEmpty()) {
            throw new NoSuchElementException("Physical test not found");
        }
        PhysicalTest pt = matches.get(0);
        return toDto(pt);
    }

    @Transactional
    public PhysicalTestDto upsert(PhysicalTestUpsertRequest request) {
        if (request == null || request.getApplicationNo() == null) {
            throw new IllegalArgumentException("applicationNo is required");
        }

        List<Candidate> candidates = candidateRepository.findAllByApplicationNoOrderByIdDesc(request.getApplicationNo());
        if (candidates.isEmpty()) {
            throw new NoSuchElementException("Candidate not found");
        }
        if (candidates.size() > 1) {
            log.warn("Duplicate candidates found for applicationNo={}. Using most recent by id for physical test.",
                    request.getApplicationNo());
        }
        Candidate candidate = candidates.get(0);

        PhysicalTest pt = physicalTestRepository.findByCandidateId(candidate.getId())
                .orElseGet(() -> {
                    PhysicalTest created = new PhysicalTest();
                    created.setCandidate(candidate);
                    return created;
                });

        pt.setHeight(request.getHeight());
        pt.setChest(request.getChest());
        pt.setExpandedChest(request.getExpandedChest());

        // status can be computed later; keep null unless already set
        PhysicalTest saved = physicalTestRepository.save(pt);
        return toDto(saved);
    }

    private PhysicalTestDto toDto(PhysicalTest pt) {
        Long appNo = null;
        try {
            appNo = pt.getCandidate() != null ? pt.getCandidate().getApplicationNo() : null;
        } catch (Exception ignored) {
            // candidate is LAZY; may not be initialized in some contexts
        }
        return PhysicalTestDto.builder()
                .id(pt.getId())
                .applicationNo(appNo)
                .height(pt.getHeight())
                .chest(pt.getChest())
                .expandedChest(pt.getExpandedChest())
                .status(pt.getStatus())
                .build();
    }
}

