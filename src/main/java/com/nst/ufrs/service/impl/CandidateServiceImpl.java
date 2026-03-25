package com.nst.ufrs.service.impl;

import com.nst.ufrs.domain.Candidate;
import com.nst.ufrs.dto.CandidateDetailsDto;
import com.nst.ufrs.dto.CandidateDocumentVerificationDto;
import com.nst.ufrs.dto.CandidateEnrollmentRequest;
import com.nst.ufrs.dto.CandidateListItemDto;
import com.nst.ufrs.dto.CandidateVerificationDataDto;
import com.nst.ufrs.dto.DocumentVerificationDecisionRequest;
import com.nst.ufrs.dto.ExcelUploadResponse;
import com.nst.ufrs.dto.ExcelUploadResponse.RowError;
import com.nst.ufrs.exception.ExcelParseException;
import com.nst.ufrs.exception.InvalidFileException;
import com.nst.ufrs.repository.CandidateRepository;
import com.nst.ufrs.service.CandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.Styles;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateServiceImpl implements CandidateService {

    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final int BATCH_SIZE = 500;
    private static final Object UPLOAD_LOCK = new Object();

    private final CandidateRepository candidateRepository;

    static {
        // App start hote hi set karo — ek baar kaafi hai
        IOUtils.setByteArrayMaxOverride(300_000_000);
    }

    @Override
    @Transactional
    public ExcelUploadResponse uploadCandidatesFromExcel(MultipartFile file) {

        validateFile(file);

        synchronized (UPLOAD_LOCK) {
            List<RowError> allErrors    = new ArrayList<>();
            List<Candidate> batchBuffer = new ArrayList<>(BATCH_SIZE);
            final List<Integer> batchRowNums = new ArrayList<>(BATCH_SIZE);
            final Set<Long> seenApplicationNos = new HashSet<>();
            final Set<Long> seenTokenNos = new HashSet<>();
            int[] counters = {0, 0, 0}; // [totalRows, savedCount, skippedCount]

            log.info("Starting SAX Excel upload: filename={}, size={}B", file.getOriginalFilename(), file.getSize());

            try (InputStream is = file.getInputStream(); OPCPackage pkg = OPCPackage.open(is)) {

                XSSFReader       reader        = new XSSFReader(pkg);
                SharedStrings    sharedStrings = reader.getSharedStringsTable();
                Styles           styles        = reader.getStylesTable();
                InputStream      sheetStream   = reader.getSheetsData().next(); // first sheet

                // SAX handler — row aate hi callback milta hai
                SheetContentsHandler sheetHandler = new SheetContentsHandler() {

                    private String[] currentRow = new String[77]; // 77 columns
                    private boolean  isHeader   = true;

                    @Override
                    public void startRow(int rowNum) {
                        currentRow = new String[77];
                    }

                    @Override
                    public void endRow(int rowNum) {
                        // Row 0 = header → skip
                        if (isHeader) { isHeader = false; return; }

                        // Completely empty row check
                        boolean empty = true;
                        for (String s : currentRow) {
                            if (s != null && !s.isBlank()) { empty = false; break; }
                        }
                        if (empty) { counters[2]++; return; }

                        counters[0]++; // totalRows

                        List<RowError> rowErrors = new ArrayList<>();
                        Candidate candidate = parseRow(currentRow, rowNum + 1, rowErrors);

                        if (!rowErrors.isEmpty()) {
                            allErrors.addAll(rowErrors);
                            counters[2]++; // invalid row skipped
                            return;
                        }

                        if (candidate == null || candidate.getApplicationNo() == null) {
                            allErrors.add(RowError.builder()
                                    .rowNumber(rowNum + 1)
                                    .field("ApplicationNo")
                                    .rawValue(candidate == null ? null : String.valueOf(candidate.getApplicationNo()))
                                    .reason("ApplicationNo is required")
                                    .build());
                            counters[2]++;
                            return;
                        }

                        Long appNo = candidate.getApplicationNo();
                        if (!seenApplicationNos.add(appNo)) {
                            allErrors.add(RowError.builder()
                                    .rowNumber(rowNum + 1)
                                    .field("ApplicationNo")
                                    .rawValue(String.valueOf(appNo))
                                    .reason("Duplicate ApplicationNo in uploaded file (row skipped)")
                                    .build());
                            counters[2]++;
                            return;
                        }

                        Long tokenNo = candidate.getTokenNo();
                        if (tokenNo != null && !seenTokenNos.add(tokenNo)) {
                            allErrors.add(RowError.builder()
                                    .rowNumber(rowNum + 1)
                                    .field("TokenNo")
                                    .rawValue(String.valueOf(tokenNo))
                                    .reason("Duplicate TokenNo in uploaded file (row skipped)")
                                    .build());
                            counters[2]++;
                            return;
                        }

                        batchBuffer.add(candidate);
                        batchRowNums.add(rowNum + 1);

                        // Batch flush
                        if (batchBuffer.size() >= BATCH_SIZE) {
                            flushBatch(batchBuffer, batchRowNums, counters, allErrors);
                        }
                    }

                    @Override
                    public void cell(String cellReference, String formattedValue, XSSFComment comment) {
                        if (cellReference == null || formattedValue == null) return;
                        // Column index निकालो (A=0, B=1 ...)
                        int colIdx = new CellReference(cellReference).getCol();
                        if (colIdx < 77) {
                            currentRow[colIdx] = formattedValue.trim();
                        }
                    }
                };

                // SAX parser setup
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XMLReader xmlReader = factory.newSAXParser().getXMLReader();

                ContentHandler handler = new XSSFSheetXMLHandler(
                        styles, null, sharedStrings, sheetHandler, new org.apache.poi.ss.usermodel.DataFormatter(), false
                );
                xmlReader.setContentHandler(handler);
                xmlReader.parse(new InputSource(sheetStream));
                sheetStream.close();

                // Remaining records save karo
                if (!batchBuffer.isEmpty()) {
                    flushBatch(batchBuffer, batchRowNums, counters, allErrors);
                }

            } catch (InvalidFileException | ExcelParseException ex) {
                throw ex;
            } catch (Exception ex) {
                log.error("Fatal error during SAX Excel processing: {}", ex.getMessage(), ex);
                throw new ExcelParseException("Failed to process Excel file: " + ex.getMessage(), ex);
            }

            log.info("Upload done. totalRows={}, saved={}, skipped={}, errors={}",
                    counters[0], counters[1], counters[2], allErrors.size());

            return ExcelUploadResponse.builder()
                    .success(true)
                    .message(String.format("Upload complete. %d records saved successfully.", counters[1]))
                    .totalRowsRead(counters[0])
                    .savedCount(counters[1])
                    .skippedCount(counters[2])
                    .errorCount(allErrors.size())
                    .errors(allErrors)
                    .build();
        }
    }

    private void flushBatch(List<Candidate> batchBuffer,
                            List<Integer> batchRowNums,
                            int[] counters,
                            List<RowError> allErrors) {
        if (batchBuffer.isEmpty()) return;

        List<Long> appNos = new ArrayList<>(batchBuffer.size());
        List<Long> tokenNos = new ArrayList<>(batchBuffer.size());
        for (Candidate c : batchBuffer) {
            if (c.getApplicationNo() != null) appNos.add(c.getApplicationNo());
            if (c.getTokenNo() != null) tokenNos.add(c.getTokenNo());
        }

        Set<Long> existingAppNos = new HashSet<>();
        if (!appNos.isEmpty()) {
            existingAppNos.addAll(candidateRepository.findExistingApplicationNos(appNos));
        }

        Set<Long> existingTokenNos = new HashSet<>();
        if (!tokenNos.isEmpty()) {
            existingTokenNos.addAll(candidateRepository.findExistingTokenNos(tokenNos));
        }

        List<Candidate> toSave = new ArrayList<>(batchBuffer.size());
        for (int i = 0; i < batchBuffer.size(); i++) {
            Candidate c = batchBuffer.get(i);
            int rowNum = batchRowNums.get(i);

            Long appNo = c.getApplicationNo();
            if (appNo != null && existingAppNos.contains(appNo)) {
                counters[2]++;
                allErrors.add(RowError.builder()
                        .rowNumber(rowNum)
                        .field("ApplicationNo")
                        .rawValue(String.valueOf(appNo))
                        .reason("Duplicate ApplicationNo already exists in database (row skipped)")
                        .build());
                continue;
            }

            Long tokenNo = c.getTokenNo();
            if (tokenNo != null && existingTokenNos.contains(tokenNo)) {
                counters[2]++;
                allErrors.add(RowError.builder()
                        .rowNumber(rowNum)
                        .field("TokenNo")
                        .rawValue(String.valueOf(tokenNo))
                        .reason("Duplicate TokenNo already exists in database (row skipped)")
                        .build());
                continue;
            }

            toSave.add(c);
        }

        if (!toSave.isEmpty()) {
            candidateRepository.saveAll(toSave);
            counters[1] += toSave.size();
            log.debug("Flushed batch. Batch size={}, savedNow={}, totalSaved={}",
                    batchBuffer.size(), toSave.size(), counters[1]);
        } else {
            log.debug("Flushed batch. Batch size={}, savedNow=0 (all skipped)", batchBuffer.size());
        }

        batchBuffer.clear();
        batchRowNums.clear();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Row parser — String[] array se Candidate बनाओ
    // ─────────────────────────────────────────────────────────────────────────
    private Candidate parseRow(String[] col, int displayRow, List<RowError> errors) {
        return Candidate.builder()
                .srNo(             parseInt(col[0]))
                .username(                  col[1])
                .tokenNo(          parseLong(col[2],  "TokenNo",       displayRow, errors))
                .applicationNo(    parseLong(col[3],  "ApplicationNo", displayRow, errors))
                .examFee(          parseDouble(col[4]))
                .post(                      col[5])
                .unitName(                  col[6])
                .firstName(                 col[7])
                .fatherName(                col[8])
                .surname(                   col[9])
                .motherName(                col[10])
                .gender(                    col[11])
                .dob(              parseDate(col[12]))
                .emailId(                   col[13])
                .mobileNo(         parseLong(col[14], "MobileNo", displayRow, null))
                .religion(                  col[15])
                .caste(                     col[16])
                .subCaste(                  col[17])
                .applicationCategory(       col[18])
                .parallelReservation(       col[19])
                .nonCremelayer(             parseYesNo(col[20]))
                .maharashtraDomicile(       parseYesNo(col[21]))
                .maharashtraDomicileCertNo( col[22])
                .maharashtraDomicileDate(   parseDate(col[23]))
                .karnatakaDomicile(         parseYesNo(col[24]))
                .karnatakaDomicileCertNo(   col[25])
                .karnatakaDomicileDate(     parseDate(col[26]))
                .exSoldier(                 parseYesNo(col[27]))
                .homeGuard(                 parseYesNo(col[28]))
                .prakalpgrast(              parseYesNo(col[29]))
                .bhukampgrast(              parseYesNo(col[30]))
                .sportsperson(              parseYesNo(col[31]))
                .parttime(                  parseYesNo(col[32]))
                .femaleReservation(         parseYesNo(col[33]))
                .parentInPolice(            parseYesNo(col[34]))
                .policeRank(                col[35])
                .policeNatureOfEmployment(  col[36])
                .policeDetails(             col[37])
                .anath(                     parseYesNo(col[38]))
                .anathDate(        parseDate(col[39]))
                .anathCertificateType(      col[40])
                .exServiceDependent(        parseYesNo(col[41]))
                .isNcc(                     parseYesNo(col[42]))
                .nccCertificateNo(          col[43])
                .nccDate(          parseDate(col[44]))
                .naxaliteArea(              parseYesNo(col[45]))
                .smallVehicle(              parseYesNo(col[46]))
                .exServiceJoiningDate(parseDate(col[47]))
                .casteCertificateNo(        col[48])
                .casteCertificateDate(parseDate(col[49]))
                .workOnContract(            parseYesNo(col[50]))
                .applicationDate(  parseDate(col[51]))
                .place(                     col[52])
                .sscBoardName(              col[53])
                .sscResult(                 col[54])
                .sscMarksObtained(  parseInt(col[55]))
                .sscTotalMarks(     parseInt(col[56]))
                .hscBoardName(              col[57])
                .hscResult(                 col[58])
                .hscMarksObtained(  parseInt(col[59]))
                .hscTotalMarks(     parseInt(col[60]))
                .seventhBoardName(          col[61])
                .seventhResult(             col[62])
                .seventhMarksObtained(parseInt(col[63]))
                .seventhTotalMarks( parseInt(col[64]))
                .diplomaBoardName(          col[65])
                .diplomaResult(             col[66])
                .diplomaMarksObtained(parseInt(col[67]))
                .diplomaTotalMarks( parseInt(col[68]))
                .mscit(                     parseYesNo(col[69]))
                .graduationDegree(          col[70])
                .postGraduationDegree(      col[71])
                .otherGraduationDegree(     col[72])
                .otherPostGraduationDegree( col[73])
                .isFarmerSuicide(           parseYesNo(col[74]))
                .farmerSuicideReportNo(     col[75])
                .farmerSuicideReportDate(parseDate(col[76]))
                .documentStatus(false)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Safe type converters
    // ─────────────────────────────────────────────────────────────────────────
    private Long parseLong(String val, String field, int row, List<RowError> errors) {
        if (val == null || val.isBlank()) return null;
        String clean = val.replaceAll("^'+", "").replaceAll("[^0-9]", "").trim();
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            if (errors != null) {
                errors.add(RowError.builder()
                        .rowNumber(row).field(field).rawValue(val)
                        .reason("Invalid numeric value").build());
            }
            log.warn("Row {}: Invalid Long for '{}': '{}'", row, field, val);
            return null;
        }
    }

    private Integer parseInt(String val) {
        if (val == null || val.isBlank()) return null;
        try { return Integer.parseInt(val.trim().replaceAll("\\.0*$", "")); }
        catch (NumberFormatException e) { return null; }
    }

    /**
     * Converts Excel "yes"/"no" (case-insensitive) to Boolean true/false.
     * Blank or other values return null.
     */
    private static Boolean parseYesNo(String val) {
        if (val == null || val.isBlank()) return null;
        String v = val.trim().toLowerCase();
        if ("yes".equals(v) || "y".equals(v) || "1".equals(v)) return true;
        if ("no".equals(v) || "n".equals(v) || "0".equals(v)) return false;
        return null;
    }

    private Double parseDouble(String val) {
        if (val == null || val.isBlank()) return null;
        try { return Double.parseDouble(val.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    /**
     * Excel / POI DataFormatter often returns dates with 2-digit year (e.g. 02/05/85).
     * This normalizes such strings to 4-digit year: 00-29 → 2000-2029, 30-99 → 1930-1999,
     * so that parseDate can parse them correctly as LocalDate.
     */
    private static final Pattern DATE_2_DIGIT_YEAR = Pattern.compile("^(\\d{1,2})([/-])(\\d{1,2})\\2(\\d{2})$");

    private static String normalizeDateWith2DigitYear(String val) {
        if (val == null || val.isBlank()) return val;
        val = val.trim();
        Matcher m = DATE_2_DIGIT_YEAR.matcher(val);
        if (!m.matches()) return val;
        int yy = Integer.parseInt(m.group(4));
        int yyyy = (yy <= 29) ? (2000 + yy) : (1900 + yy);
        String sep = m.group(2);
        return m.group(1) + sep + m.group(3) + sep + yyyy;
    }

    private java.time.LocalDate parseDate(String val) {
        if (val == null || val.isBlank()) return null;
        String trimmed = val.trim();
        try {
            for (java.time.format.DateTimeFormatter fmt : DATE_FORMATS) {
                try { return java.time.LocalDate.parse(trimmed, fmt); }
                catch (Exception ignored) {}
            }
            // Excel often formats dates with 2-digit year (e.g. 02/05/85). Normalize to 4-digit and retry.
            String normalized = normalizeDateWith2DigitYear(trimmed);
            if (!normalized.equals(trimmed)) {
                for (java.time.format.DateTimeFormatter fmt : DATE_FORMATS) {
                    try { return java.time.LocalDate.parse(normalized, fmt); }
                    catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    /** 4-digit year formatters. 2-digit year strings (e.g. 02/05/85) are normalized to 4-digit in parseDate first. */
    private static final List<java.time.format.DateTimeFormatter> DATE_FORMATS = List.of(
            java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            java.time.format.DateTimeFormatter.ofPattern("d-MMM-yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy")
    );

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new InvalidFileException("Uploaded file is empty or missing.");
        String name = file.getOriginalFilename();
        String type = file.getContentType();
        boolean validExt  = name != null && name.toLowerCase().endsWith(".xlsx");
        boolean validType = EXCEL_CONTENT_TYPE.equals(type);
        if (!validExt && !validType)
            throw new InvalidFileException("Only .xlsx files are accepted. Received: " + type);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Read/search APIs for admin UI
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CandidateListItemDto> getRecentCandidates(int limit) {
        var pageable = PageRequest.of(0, Math.max(limit, 1));
        List<Candidate> candidates = candidateRepository.findRecentCandidatesLimited(pageable);
        return mapToListItems(candidates);
    }

    List<Candidate> all = null;
    @Override
    @Transactional(readOnly = true)
    public List<CandidateListItemDto>   searchCandidates(String applicationNoText, Long mobileNo, String name, int limit) {

        if(all==null || all.isEmpty()){
            all = candidateRepository.findAll();
        }
        //List<Candidate> all = candidateRepository.findAll();
        String appPrefix = applicationNoText != null ? applicationNoText.trim() : "";
        boolean hasAppNo = !appPrefix.isEmpty();
        boolean hasMobile = mobileNo != null;
        boolean hasName = name != null && !name.trim().isEmpty();

        if (!hasAppNo && !hasMobile && !hasName) {
            return mapToListItems(all);
        }

        List<Candidate> filtered = all.stream()
                .filter(c -> {
                    if (!hasAppNo || c.getApplicationNo() == null) return true;
                    String appStr = c.getApplicationNo().toString();
                    return appStr.startsWith(appPrefix);
                })
                .filter(c -> {
                    if (!hasMobile) return true;
                    return mobileNo.equals(c.getMobileNo());
                })
                .filter(c -> {
                    if (!hasName) return true;
                    String fullName = (c.getFirstName() == null ? "" : c.getFirstName()) + " " +
                                      (c.getSurname() == null ? "" : c.getSurname());
                    return fullName.toLowerCase().contains(name.trim().toLowerCase());
                })
                .limit(Math.max(limit, 1))
                .toList();

        return mapToListItems(filtered);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateDetailsDto getCandidateDetailsByApplicationNo(long applicationNo) {
        List<Candidate> matches = candidateRepository.findAllByApplicationNoOrderByIdDesc(applicationNo);
        if (matches.isEmpty()) {
            throw new NoSuchElementException("Candidate not found");
        }
        if (matches.size() > 1) {
            log.warn("Duplicate candidates found for applicationNo={}. Using most recent by id.", applicationNo);
        }
        Candidate c = matches.get(0);

        String name = buildFullName(c.getFirstName(), c.getFatherName(), c.getSurname());
        Integer age = c.getDob() != null ? Period.between(c.getDob(), LocalDate.now()).getYears() : null;

        return CandidateDetailsDto.builder()
                .applicationNo(c.getApplicationNo())
                .name(name)
                .age(age)
                .email(c.getEmailId())
                .religion(c.getReligion())
                .post(c.getPost())
                .gender(c.getGender())
                .dob(c.getDob())
                .applicationCategory(c.getApplicationCategory())
                .parallelReservation(c.getParallelReservation())
                .mobileNo(c.getMobileNo())
                .hasPhoto(c.getPhoto() != null && !c.getPhoto().isBlank())
                .hasBiometric1(c.getBiometric1() != null && !c.getBiometric1().isBlank())
                .hasBiometric2(c.getBiometric2() != null && !c.getBiometric2().isBlank())
                .assignRunningNumberStatus(c.getAssignRunningNumberStatus())
                .runningNumber(c.getRunningNumber())
                .physicalTestStatus(c.getPhysicalTestStatus())
                .attendance(c.getAttendance())
                .build();
    }

    private static String buildFullName(String firstName, String fatherName, String surname) {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) sb.append(firstName.trim());
        if (fatherName != null && !fatherName.isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(fatherName.trim());
        }
        if (surname != null && !surname.isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(surname.trim());
        }
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateVerificationDataDto getCandidateVerificationData(long applicationNo) {
        List<Candidate> matches = candidateRepository.findAllByApplicationNoOrderByIdDesc(applicationNo);
        if (matches.isEmpty()) {
            throw new NoSuchElementException("Candidate not found");
        }
        if (matches.size() > 1) {
            log.warn("Duplicate candidates found for applicationNo={}. Using most recent by id for verification data.", applicationNo);
        }
        Candidate c = matches.get(0);

        return CandidateVerificationDataDto.builder()
                .applicationNo(c.getApplicationNo())
                .photo(c.getPhoto())
                .biometric1(c.getBiometric1())
                .biometric2(c.getBiometric2())
                .build();
    }

    @Override
    @Transactional
    public void enrollCandidate(CandidateEnrollmentRequest request) {
        if (request == null || request.getApplicationNo() == null) {
            throw new IllegalArgumentException("applicationNo is required");
        }
        if (request.getPhoto() == null || request.getPhoto().isBlank()) {
            throw new IllegalArgumentException("photo is required");
        }
        if (request.getBiometric1() == null || request.getBiometric1().isBlank()) {
            throw new IllegalArgumentException("biometric1 is required");
        }
        if (request.getBiometric2() == null || request.getBiometric2().isBlank()) {
            throw new IllegalArgumentException("biometric2 is required");
        }

        List<Candidate> matches = candidateRepository.findAllByApplicationNoOrderByIdDesc(request.getApplicationNo());
        if (matches.isEmpty()) {
            throw new NoSuchElementException("Candidate not found");
        }
        if (matches.size() > 1) {
            log.warn("Duplicate candidates found for applicationNo={}. Using most recent by id for enrollment.",
                    request.getApplicationNo());
        }
        Candidate c = matches.get(0);

        c.setPhoto(request.getPhoto());
        c.setBiometric1(request.getBiometric1());
        c.setBiometric2(request.getBiometric2());
        c.setAttendance(true);

        candidateRepository.save(c);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateDocumentVerificationDto getCandidateDocumentVerificationData(long applicationNo) {
        List<Candidate> matches = candidateRepository.findAllByApplicationNoOrderByIdDesc(applicationNo);
        if (matches.isEmpty()) {
            throw new NoSuchElementException("Candidate not found");
        }
        if (matches.size() > 1) {
            log.warn("Duplicate candidates found for applicationNo={}. Using most recent by id for document verification data.", applicationNo);
        }
        Candidate c = matches.get(0);

        // Ensure candidate has completed enrollment (photo + biometrics captured)
        if (c.getAttendance() == null || !c.getAttendance()) {
            throw new IllegalStateException("Candidate has not completed photo and biometric enrollment. Please complete Add Candidate first.");
        }

        String name = buildFullName(c.getFirstName(), c.getFatherName(), c.getSurname());
        Integer age = c.getDob() != null ? Period.between(c.getDob(), LocalDate.now()).getYears() : null;

        return CandidateDocumentVerificationDto.builder()
                .applicationNo(c.getApplicationNo())
                .name(name)
                .gender(c.getGender())
                .mobileNo(c.getMobileNo())
                .dob(c.getDob())
                .age(age)
                .email(c.getEmailId())
                .post(c.getPost())
                .religion(c.getReligion())
                .applicationCategory(c.getApplicationCategory())
                .parallelReservation(c.getParallelReservation())
                .photo(c.getPhoto())
                .naxaliteArea(c.getNaxaliteArea())
                .documentStatus(c.getDocumentStatus())
                .nonCremelayer(c.getNonCremelayer())
                .maharashtraDomicile(c.getMaharashtraDomicile())
                .karnatakaDomicile(c.getKarnatakaDomicile())
                .exSoldier(c.getExSoldier())
                .homeGuard(c.getHomeGuard())
                .prakalpgrast(c.getPrakalpgrast())
                .bhukampgrast(c.getBhukampgrast())
                .sportsperson(c.getSportsperson())
                .femaleReservation(c.getFemaleReservation())
                .parentInPolice(c.getParentInPolice())
                .anath(c.getAnath())
                .exServiceDependent(c.getExServiceDependent())
                .isNcc(c.getIsNcc())
                .smallVehicle(c.getSmallVehicle())
                .workOnContract(c.getWorkOnContract())
                .mscit(c.getMscit())
                .isFarmerSuicide(c.getIsFarmerSuicide())
                .build();
    }

    @Override
    @Transactional
    public void applyDocumentVerificationDecision(DocumentVerificationDecisionRequest request) {
        if (request == null || request.getApplicationNo() == null) {
            throw new IllegalArgumentException("applicationNo is required");
        }

        List<Candidate> matches = candidateRepository.findAllByApplicationNoOrderByIdDesc(request.getApplicationNo());
        if (matches.isEmpty()) {
            throw new NoSuchElementException("Candidate not found");
        }
        if (matches.size() > 1) {
            log.warn("Duplicate candidates found for applicationNo={}. Using most recent by id for document verification decision.",
                    request.getApplicationNo());
        }
        Candidate c = matches.get(0);

        c.setDocumentStatus(request.isAllRequiredVerified());
        // When documents are missing, we keep overall status=false (rejected) which is already default/null in many flows

        candidateRepository.save(c);
    }

    private List<CandidateListItemDto> mapToListItems(List<Candidate> candidates) {
        List<CandidateListItemDto> result = new ArrayList<>(candidates.size());
        LocalDate today = LocalDate.now();

        for (Candidate c : candidates) {
            Integer age = null;
            if (c.getDob() != null) {
                age = Period.between(c.getDob(), today).getYears();
            }

            String fullName = String.format("%s %s",
                    c.getFirstName() != null ? c.getFirstName() : "",
                    c.getSurname() != null ? c.getSurname() : "").trim();

            result.add(CandidateListItemDto.builder()
                    .id(c.getId())
                    .applicationNo(c.getApplicationNo())
                    .tokenNo(c.getTokenNo())
                    .name(fullName.isEmpty() ? c.getUsername() : fullName)
                    .mobileNo(c.getMobileNo())
                    .category(c.getApplicationCategory())
                    .build());
        }
        return result;
    }
}
