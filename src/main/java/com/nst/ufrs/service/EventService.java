package com.nst.ufrs.service;

import com.nst.ufrs.domain.Event;
import com.nst.ufrs.dto.ExcelUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EventService {

    /**
     * Upload and parse an Excel (.xlsx) file containing event result rows and persist them.
     *
     * @param file           uploaded Excel file
     * @param name           numeric event name (e.g. 100, 5)
     * @param unit           unit for the event ("m", "km")
     * @param lap            lap value to be stored on each row
     * @param eventLocationId eventLocation.id from session
     */
    ExcelUploadResponse uploadEventsFromExcel(
            MultipartFile file,
            int name,
            String unit,
            int lap,
            Long eventLocationId
    );

    /**
     * Search or list event records for UI.
     *
     * @param runningNumber  optional chest/running number filter
     * @param limit          max number of rows to return
     * @param eventLocationId eventLocation.id from session
     */
    List<Event> searchEvents(Integer runningNumber, int limit, Long eventLocationId);
}

