package com.nst.ufrs.service.impl;

import com.nst.ufrs.domain.Candidate;
import com.nst.ufrs.domain.Event;
import com.nst.ufrs.domain.Shotput;
import com.nst.ufrs.dto.CandidateMasterRowDto;
import com.nst.ufrs.repository.CandidateRepository;
import com.nst.ufrs.repository.EventRepository;
import com.nst.ufrs.repository.ShotputRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateMasterService {

    private final CandidateRepository candidateRepository;
    private final EventRepository eventRepository;
    private final ShotputRepository shotputRepository;

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional(readOnly = true)
    public Page<CandidateMasterRowDto> search(
            LocalDate fromDate,
            LocalDate toDate,
            String statusType,
            Boolean approved,
            int page,
            int pageSize,
            Long eventLocationId
    ) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(pageSize, 1));
        Page<Candidate> pageResult = candidateRepository.searchForCandidateMaster(fromDate, toDate, statusType, approved, pageable);
        List<CandidateMasterRowDto> rows = mapCandidates(pageResult.getContent(), eventLocationId);
        return pageResult.map(c -> rows.get(pageResult.getContent().indexOf(c)));
    }

    @Transactional(readOnly = true)
    public byte[] export(
            LocalDate fromDate,
            LocalDate toDate,
            String statusType,
            Boolean approved,
            Long eventLocationId,
            boolean includeResultColumn
    ) {
        // Export all rows (no paging) for the given filter
        Page<Candidate> page = candidateRepository.searchForCandidateMaster(fromDate, toDate, statusType, approved,
                PageRequest.of(0, Integer.MAX_VALUE));
        List<CandidateMasterRowDto> rows = mapCandidates(page.getContent(), eventLocationId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Result");

            int col = 0;
            Row header = sheet.createRow(0);
            header.createCell(col++).setCellValue("Sr No");
            header.createCell(col++).setCellValue("Token No");
            header.createCell(col++).setCellValue("Application No");
            header.createCell(col++).setCellValue("Post");
            header.createCell(col++).setCellValue("First Name");
            header.createCell(col++).setCellValue("Father Name");
            header.createCell(col++).setCellValue("Surname");
            header.createCell(col++).setCellValue("Mother Name");
            header.createCell(col++).setCellValue("DOB");
            header.createCell(col++).setCellValue("Age");
            header.createCell(col++).setCellValue("Gender");
            header.createCell(col++).setCellValue("Category");
            header.createCell(col++).setCellValue("Reservation Type");
            header.createCell(col++).setCellValue("Mobile No");
            header.createCell(col++).setCellValue("Result");
            header.createCell(col++).setCellValue("100m Start Time");
            header.createCell(col++).setCellValue("100m End Time");
            header.createCell(col++).setCellValue("100m Time Difference");
            header.createCell(col++).setCellValue("100m Marks");
            header.createCell(col++).setCellValue("5km Start Time");
            header.createCell(col++).setCellValue("5km End Time");
            header.createCell(col++).setCellValue("5km Time Difference");
            header.createCell(col++).setCellValue("5km Marks");
            header.createCell(col++).setCellValue("Shotput Attempt1");
            header.createCell(col++).setCellValue("Shotput Attempt2");
            header.createCell(col++).setCellValue("Shotput Attempt3");
            header.createCell(col++).setCellValue("Shotput Highest Distance");
            header.createCell(col++).setCellValue("Shotput Marks");
            header.createCell(col++).setCellValue("Total");
            if (includeResultColumn) {
                header.createCell(col).setCellValue("Result (Pass/Fail)");
            }

            int rowIdx = 1;
            for (CandidateMasterRowDto r : rows) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;
                setLong(row, c++, r.getSrNo() != null ? r.getSrNo().longValue() : null);
                setLong(row, c++, r.getTokenNo());
                setLong(row, c++, r.getApplicationNo());
                setString(row, c++, r.getPost());
                setString(row, c++, r.getFirstName());
                setString(row, c++, r.getFatherName());
                setString(row, c++, r.getSurname());
                setString(row, c++, r.getMotherName());
                setString(row, c++, r.getDob() != null ? r.getDob().toString() : null);
                setLong(row, c++, r.getAge() != null ? r.getAge().longValue() : null);
                setString(row, c++, r.getGender());
                setString(row, c++, r.getCategory());
                setString(row, c++, r.getReservationType());
                setLong(row, c++, r.getMobileNo());
                setString(row, c++, r.getResultStatus() == null ? "" : (r.getResultStatus() ? "Pass" : "Fail"));
                setString(row, c++, r.getHundredStartTime() != null ? r.getHundredStartTime().format(DATE_TIME_FMT) : null);
                setString(row, c++, r.getHundredEndTime() != null ? r.getHundredEndTime().format(DATE_TIME_FMT) : null);
                setDouble(row, c++, r.getHundredTimeDiff());
                setDouble(row, c++, r.getHundredMarks());
                setString(row, c++, r.getFiveKmStartTime() != null ? r.getFiveKmStartTime().format(DATE_TIME_FMT) : null);
                setString(row, c++, r.getFiveKmEndTime() != null ? r.getFiveKmEndTime().format(DATE_TIME_FMT) : null);
                setDouble(row, c++, r.getFiveKmTimeDiff());
                setDouble(row, c++, r.getFiveKmMarks());
                setDouble(row, c++, r.getShotputAttempt1() != null ? r.getShotputAttempt1().doubleValue() : null);
                setDouble(row, c++, r.getShotputAttempt2() != null ? r.getShotputAttempt2().doubleValue() : null);
                setDouble(row, c++, r.getShotputAttempt3() != null ? r.getShotputAttempt3().doubleValue() : null);
                setDouble(row, c++, r.getShotputHighestDistance() != null ? r.getShotputHighestDistance().doubleValue() : null);
                setLong(row, c++, r.getShotputMarks() != null ? r.getShotputMarks().longValue() : null);
                setDouble(row, c++, r.getTotalMarks());
                if (includeResultColumn) {
                    setString(row, c, r.getResultStatus() == null ? "" : (r.getResultStatus() ? "Pass" : "Fail"));
                }
            }

            for (int i = 0; i <= (includeResultColumn ? 29 : 28); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate candidate master Excel: " + e.getMessage(), e);
        }
    }

    private List<CandidateMasterRowDto> mapCandidates(List<Candidate> candidates, Long eventLocationId) {
        if (candidates.isEmpty()) return List.of();

        Map<Integer, Event> hundredByRunning = Collections.emptyMap();
        Map<Integer, Event> fiveKmByRunning = Collections.emptyMap();
        if (eventLocationId != null) {
            Set<Integer> runningNumbers = candidates.stream()
                    .map(Candidate::getRunningNumber)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!runningNumbers.isEmpty()) {
                List<Event> hundred = eventRepository.findAllByEventLocationIdAndNameAndUnitAndRunningNumberIn(
                        eventLocationId, 100, "m", runningNumbers);
                hundredByRunning = latestByRunning(hundred);
                List<Event> fiveKm = eventRepository.findAllByEventLocationIdAndNameAndUnitAndRunningNumberIn(
                        eventLocationId, 5, "km", runningNumbers);
                fiveKmByRunning = latestByRunning(fiveKm);
            }
        }

        Map<Long, Shotput> shotputByCandidate = Collections.emptyMap();
        List<Long> candidateIds = candidates.stream().map(Candidate::getId).filter(Objects::nonNull).toList();
        if (!candidateIds.isEmpty()) {
            List<Shotput> allShotput = shotputRepository.findAllByCandidateIdIn(candidateIds);
            Map<Long, Shotput> tmp = new HashMap<>();
            for (Shotput s : allShotput) {
                Long cid = s.getCandidate() != null ? s.getCandidate().getId() : null;
                if (cid == null) continue;
                Shotput existing = tmp.get(cid);
                if (existing == null || s.getId() > existing.getId()) {
                    tmp.put(cid, s);
                }
            }
            shotputByCandidate = tmp;
        }

        List<CandidateMasterRowDto> result = new ArrayList<>(candidates.size());
        for (Candidate c : candidates) {
            Integer age = c.getDob() != null ? Period.between(c.getDob(), LocalDate.now()).getYears() : null;
            Integer runningNo = c.getRunningNumber();

            Event e100 = runningNo != null ? hundredByRunning.get(runningNo) : null;
            Event e5 = runningNo != null ? fiveKmByRunning.get(runningNo) : null;
            Shotput s = c.getId() != null ? shotputByCandidate.get(c.getId()) : null;

            double total = 0d;
            if (e100 != null && e100.getMarks() != null) total += e100.getMarks();
            if (e5 != null && e5.getMarks() != null) total += e5.getMarks();
            if (s != null && s.getHighestMarks() != null) total += s.getHighestMarks();

            CandidateMasterRowDto dto = CandidateMasterRowDto.builder()
                    .srNo(c.getSrNo())
                    .tokenNo(c.getTokenNo())
                    .applicationNo(c.getApplicationNo())
                    .post(c.getPost())
                    .firstName(c.getFirstName())
                    .fatherName(c.getFatherName())
                    .surname(c.getSurname())
                    .motherName(c.getMotherName())
                    .dob(c.getDob())
                    .age(age)
                    .gender(c.getGender())
                    .category(c.getApplicationCategory())
                    .reservationType(c.getParallelReservation())
                    .mobileNo(c.getMobileNo())
                    .resultStatus(c.getResultStatus())
                    .hundredStartTime(e100 != null ? e100.getStartTime() : null)
                    .hundredEndTime(e100 != null ? e100.getEndTime() : null)
                    .hundredTimeDiff(e100 != null ? e100.getTimeDifference() : null)
                    .hundredMarks(e100 != null ? e100.getMarks() : null)
                    .fiveKmStartTime(e5 != null ? e5.getStartTime() : null)
                    .fiveKmEndTime(e5 != null ? e5.getEndTime() : null)
                    .fiveKmTimeDiff(e5 != null ? e5.getTimeDifference() : null)
                    .fiveKmMarks(e5 != null ? e5.getMarks() : null)
                    .shotputAttempt1(s != null ? s.getAttempt1() : null)
                    .shotputAttempt2(s != null ? s.getAttempt2() : null)
                    .shotputAttempt3(s != null ? s.getAttempt3() : null)
                    .shotputHighestDistance(s != null ? s.getHighestDistance() : null)
                    .shotputMarks(s != null ? s.getHighestMarks() : null)
                    .totalMarks(total == 0d ? null : total)
                    .build();
            result.add(dto);
        }
        return result;
    }

    private static Map<Integer, Event> latestByRunning(List<Event> events) {
        Map<Integer, Event> map = new HashMap<>();
        for (Event e : events) {
            Integer running = e.getRunningNumber();
            if (running == null) continue;
            Event existing = map.get(running);
            if (existing == null || e.getId() > existing.getId()) {
                map.put(running, e);
            }
        }
        return map;
    }

    private static void setString(Row row, int col, String value) {
        if (value == null) return;
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
    }

    private static void setLong(Row row, int col, Long value) {
        if (value == null) return;
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
    }

    private static void setDouble(Row row, int col, Double value) {
        if (value == null) return;
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
    }
}

