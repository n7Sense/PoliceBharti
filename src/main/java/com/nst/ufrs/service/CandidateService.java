package com.nst.ufrs.service;

import com.nst.ufrs.dto.CandidateListItemDto;
import com.nst.ufrs.dto.ExcelUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service contract for candidate Excel upload operations and read/search APIs.
 */
public interface CandidateService {

    /**
     * Parses the given Excel file, validates each row,
     * performs bulk save to MySQL, and returns a detailed response.
     *
     * @param file the uploaded .xlsx file
     * @return ExcelUploadResponse with counts and error details
     */
    ExcelUploadResponse uploadCandidatesFromExcel(MultipartFile file);

    /**
     * Returns a limited list of most recently created candidates for the admin table.
     *
     * @param limit max records to return
     */
    List<CandidateListItemDto> getRecentCandidates(int limit);

    /**
     * Searches candidates by optional application number, mobile number and name.
     * All parameters are optional; if all are null/blank, recent candidates are returned.
     */
    List<CandidateListItemDto> searchCandidates(Long applicationNo, Long mobileNo, String name, int limit);
}