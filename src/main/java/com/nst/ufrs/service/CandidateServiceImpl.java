package com.nst.ufrs.service;

import com.nst.ufrs.domain.Candidate;
import com.nst.ufrs.dto.CandidateDetailsDto;
import com.nst.ufrs.dto.CandidateEnrollmentRequest;
import com.nst.ufrs.dto.CandidateListItemDto;
import com.nst.ufrs.dto.ExcelUploadResponse;
import com.nst.ufrs.dto.ExcelUploadResponse.RowError;
import com.nst.ufrs.exception.ExcelParseException;
import com.nst.ufrs.exception.InvalidFileException;
import com.nst.ufrs.repository.CandidateRepository;
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
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateServiceImpl implements CandidateService {

    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final int BATCH_SIZE = 500;

    private final CandidateRepository candidateRepository;

    static {
        // App start hote hi set karo — ek baar kaafi hai
        IOUtils.setByteArrayMaxOverride(300_000_000);
    }

    @Override
    @Transactional
    public ExcelUploadResponse uploadCandidatesFromExcel(MultipartFile file) {

        validateFile(file);

        List<RowError> allErrors    = new ArrayList<>();
        List<Candidate> batchBuffer = new ArrayList<>(BATCH_SIZE);
        int[] counters = {0, 0, 0}; // [totalRows, savedCount, skippedCount]

        log.info("Starting SAX Excel upload: filename={}, size={}B",
                file.getOriginalFilename(), file.getSize());

        try (InputStream is = file.getInputStream();
             OPCPackage pkg = OPCPackage.open(is)) {

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

                    if (!rowErrors.isEmpty()) allErrors.addAll(rowErrors);
                    batchBuffer.add(candidate);

                    // Batch flush
                    if (batchBuffer.size() >= BATCH_SIZE) {
                        candidateRepository.saveAll(batchBuffer);
                        counters[1] += batchBuffer.size();
                        log.debug("Flushed batch. Total saved: {}", counters[1]);
                        batchBuffer.clear();
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
                candidateRepository.saveAll(batchBuffer);
                counters[1] += batchBuffer.size();
                batchBuffer.clear();
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
                .nonCremelayer(             col[20])
                .maharashtraDomicile(       col[21])
                .maharashtraDomicileCertNo( col[22])
                .maharashtraDomicileDate(   parseDate(col[23]))
                .karnatakaDomicile(         col[24])
                .karnatakaDomicileCertNo(   col[25])
                .karnatakaDomicileDate(     parseDate(col[26]))
                .exSoldier(                 col[27])
                .homeGuard(                 col[28])
                .prakalpgrast(              col[29])
                .bhukampgrast(              col[30])
                .sportsperson(              col[31])
                .parttime(                  col[32])
                .femaleReservation(         col[33])
                .parentInPolice(            col[34])
                .policeRank(                col[35])
                .policeNatureOfEmployment(  col[36])
                .policeDetails(             col[37])
                .anath(                     col[38])
                .anathDate(        parseDate(col[39]))
                .anathCertificateType(      col[40])
                .exServiceDependent(        col[41])
                .isNcc(                     col[42])
                .nccCertificateNo(          col[43])
                .nccDate(          parseDate(col[44]))
                .naxaliteArea(              col[45])
                .smallVehicle(              col[46])
                .exServiceJoiningDate(parseDate(col[47]))
                .casteCertificateNo(        col[48])
                .casteCertificateDate(parseDate(col[49]))
                .workOnContract(            col[50])
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
                .mscit(                     col[69])
                .graduationDegree(          col[70])
                .postGraduationDegree(      col[71])
                .otherGraduationDegree(     col[72])
                .otherPostGraduationDegree( col[73])
                .isFarmerSuicide(           col[74])
                .farmerSuicideReportNo(     col[75])
                .farmerSuicideReportDate(parseDate(col[76]))
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

    private Double parseDouble(String val) {
        if (val == null || val.isBlank()) return null;
        try { return Double.parseDouble(val.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private java.time.LocalDate parseDate(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            // POI DataFormatter gives dates as "dd-MMM-yyyy" or "d/M/yyyy" etc.
            for (java.time.format.DateTimeFormatter fmt : DATE_FORMATS) {
                try { return java.time.LocalDate.parse(val.trim(), fmt); }
                catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static final List<java.time.format.DateTimeFormatter> DATE_FORMATS = List.of(
            java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            java.time.format.DateTimeFormatter.ofPattern("d-MMM-yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy")
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

    @Override
    @Transactional(readOnly = true)
    public List<CandidateListItemDto> searchCandidates(String applicationNoText, Long mobileNo, String name, int limit) {
        List<Candidate> all = candidateRepository.findAll();
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
        Candidate c = candidateRepository.findByApplicationNo(applicationNo)
                .orElseThrow(() -> new NoSuchElementException("Candidate not found"));

        return CandidateDetailsDto.builder()
                .applicationNo(c.getApplicationNo())
                .post(c.getPost())
                .gender(c.getGender())
                .dob(c.getDob())
                .applicationCategory(c.getApplicationCategory())
                .parallelReservation(c.getParallelReservation())
                .mobileNo(c.getMobileNo())
                .hasPhoto(c.getPhoto() != null && !c.getPhoto().isBlank())
                .hasBiometric1(c.getBiometric1() != null && !c.getBiometric1().isBlank())
                .hasBiometric2(c.getBiometric2() != null && !c.getBiometric2().isBlank())
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

        Candidate c = candidateRepository.findByApplicationNo(request.getApplicationNo())
                .orElseThrow(() -> new NoSuchElementException("Candidate not found"));

        c.setPhoto(request.getPhoto());
        c.setBiometric1(request.getBiometric1());
        c.setBiometric2(request.getBiometric2());
        c.setAttendance(true);

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
