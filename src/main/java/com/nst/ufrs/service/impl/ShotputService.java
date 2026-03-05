package com.nst.ufrs.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.nst.ufrs.domain.Candidate;
import com.nst.ufrs.domain.Shotput;
import com.nst.ufrs.dto.ShotputDto;
import com.nst.ufrs.dto.ShotputUpsertRequest;
import com.nst.ufrs.repository.CandidateRepository;
import com.nst.ufrs.repository.ShotputRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ShotputService {

    private final CandidateRepository candidateRepository;
    private final ShotputRepository shotputRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd / MM / yyyy");

    @Transactional(readOnly = true)
    public ShotputDto getByApplicationNo(long applicationNo) {
        List<Shotput> matches = shotputRepository.findAllByApplicationNoOrderByIdDesc(applicationNo);
        if (matches.isEmpty()) {
            throw new NoSuchElementException("Shotput record not found");
        }
        Shotput s = matches.get(0);
        return toDto(s);
    }

    @Transactional
    public ShotputDto upsert(ShotputUpsertRequest request) {
        if (request == null || request.getApplicationNo() == null) {
            throw new IllegalArgumentException("applicationNo is required");
        }

        List<Candidate> candidates = candidateRepository.findAllByApplicationNoOrderByIdDesc(request.getApplicationNo());
        if (candidates.isEmpty()) {
            throw new NoSuchElementException("Candidate not found");
        }
        Candidate candidate = candidates.get(0);

        Shotput s = shotputRepository.findByCandidateId(candidate.getId())
                .orElseGet(() -> {
                    Shotput created = new Shotput();
                    created.setCandidate(candidate);
                    return created;
                });

        s.setAttempt1(request.getAttempt1());
        s.setAttempt2(request.getAttempt2());
        s.setAttempt3(request.getAttempt3());

        Integer m1 = computeMarks(request.getAttempt1());
        Integer m2 = computeMarks(request.getAttempt2());
        Integer m3 = computeMarks(request.getAttempt3());
        s.setMarks1(m1);
        s.setMarks2(m2);
        s.setMarks3(m3);

        float highestDistance = 0f;
        int highestMarks = 0;

        if (request.getAttempt1() != null && request.getAttempt1() > highestDistance) {
            highestDistance = request.getAttempt1();
            highestMarks = m1 != null ? m1 : 0;
        }
        if (request.getAttempt2() != null && request.getAttempt2() > highestDistance) {
            highestDistance = request.getAttempt2();
            highestMarks = m2 != null ? m2 : 0;
        }
        if (request.getAttempt3() != null && request.getAttempt3() > highestDistance) {
            highestDistance = request.getAttempt3();
            highestMarks = m3 != null ? m3 : 0;
        }

        s.setHighestDistance(highestDistance > 0 ? highestDistance : null);
        s.setHighestMarks(highestDistance > 0 ? highestMarks : null);

        Shotput saved = shotputRepository.save(s);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public byte[] generatePdfReport(long applicationNo) {
        Candidate candidate = resolveCandidate(applicationNo);
        Shotput s = shotputRepository.findByCandidateId(candidate.getId())
                .orElseThrow(() -> new NoSuchElementException("Shotput record not found"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 48, 48, 48, 48);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font h1 = new Font(Font.HELVETICA, 19, Font.BOLD);
            Font h2 = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font h3 = new Font(Font.HELVETICA, 14, Font.NORMAL);
            Font label = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font value = new Font(Font.HELVETICA, 11, Font.NORMAL);

            Paragraph p1 = new Paragraph("State Reserve Police Force Group No.7 Daund", h1);
            p1.setAlignment(Element.ALIGN_CENTER);
            document.add(p1);

            Paragraph p2 = new Paragraph("Armed Police Recruitment Year 2026", h2);
            p2.setAlignment(Element.ALIGN_CENTER);
            p2.setSpacingBefore(4);
            document.add(p2);

            Paragraph p3 = new Paragraph("Shotput", h3);
            p3.setAlignment(Element.ALIGN_CENTER);
            p3.setSpacingBefore(6);
            p3.setSpacingAfter(10);
            document.add(p3);

            String dateText = "Date: " + DATE_FMT.format(LocalDate.now());
            Paragraph dateP = new Paragraph(dateText, value);
            dateP.setAlignment(Element.ALIGN_LEFT);
            dateP.setSpacingAfter(10);
            document.add(dateP);

            PdfPTable top = new PdfPTable(new float[]{2.5f, 1.2f});
            top.setWidthPercentage(100);

            PdfPTable details = new PdfPTable(new float[]{1.2f, 2.0f});
            details.setWidthPercentage(100);

            addRow(details, "Application No", String.valueOf(candidate.getApplicationNo()), label, value);
            addRow(details, "Name", (safe(candidate.getFirstName()) + " " + safe(candidate.getSurname())).trim(), label, value);
            addRow(details, "Post", safe(candidate.getPost()), label, value);
            addRow(details, "Gender", safe(candidate.getGender()), label, value);
            addRow(details, "DOB", candidate.getDob() != null ? candidate.getDob().toString() : "", label, value);
            addRow(details, "Mobile", candidate.getMobileNo() != null ? candidate.getMobileNo().toString() : "", label, value);
            addRow(details, "Category", safe(candidate.getApplicationCategory()), label, value);

            PdfPCell detailsCell = new PdfPCell(details);
            detailsCell.setBorder(Rectangle.NO_BORDER);
            top.addCell(detailsCell);

            Image photo = loadCandidatePhoto(candidate.getPhoto());
            PdfPCell imgCell;
            if (photo != null) {
                photo.scaleToFit(140, 180);
                imgCell = new PdfPCell(photo, true);
            } else {
                imgCell = new PdfPCell(new Phrase("No Photo", value));
            }
            imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            imgCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            imgCell.setFixedHeight(190);
            top.addCell(imgCell);

            document.add(top);
            document.add(Chunk.NEWLINE);

            PdfPTable attempts = new PdfPTable(new float[]{1.2f, 1.2f, 1.2f});
            attempts.setWidthPercentage(70);

            addHeader(attempts, "Attempt", label);
            addHeader(attempts, "Distance (m)", label);
            addHeader(attempts, "Marks", label);

            addAttemptRow(attempts, "Attempt 1", s.getAttempt1(), s.getMarks1(), value);
            addAttemptRow(attempts, "Attempt 2", s.getAttempt2(), s.getMarks2(), value);
            addAttemptRow(attempts, "Attempt 3", s.getAttempt3(), s.getMarks3(), value);

            document.add(attempts);
            document.add(Chunk.NEWLINE);

            PdfPTable summary = new PdfPTable(new float[]{2.0f, 1.0f});
            summary.setWidthPercentage(70);

            addRow(summary, "Maximum Distance (m)",
                    s.getHighestDistance() != null ? s.getHighestDistance().toString() : "", label, value);
            addRow(summary, "Maximum Marks",
                    s.getHighestMarks() != null ? s.getHighestMarks().toString() : "", label, value);

            document.add(summary);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private Candidate resolveCandidate(long applicationNo) {
        List<Candidate> candidates = candidateRepository.findAllByApplicationNoOrderByIdDesc(applicationNo);
        if (candidates.isEmpty()) throw new NoSuchElementException("Candidate not found");
        return candidates.get(0);
    }

    private Integer computeMarks(Float attempt) {
        if (attempt == null) return null;
        float d = attempt;
        if (d >= 8.50f) return 15;
        if (d >= 7.90f) return 12;
        if (d >= 7.30f) return 10;
        if (d >= 6.70f) return 8;
        if (d >= 6.10f) return 6;
        if (d >= 5.50f) return 5;
        if (d >= 4.90f) return 4;
        if (d >= 4.30f) return 3;
        if (d >= 3.70f) return 2;
        if (d >= 3.10f) return 1;
        return 0;
    }

    private ShotputDto toDto(Shotput s) {
        Long appNo = null;
        try {
            appNo = s.getCandidate() != null ? s.getCandidate().getApplicationNo() : null;
        } catch (Exception ignored) {
        }
        return ShotputDto.builder()
                .id(s.getId())
                .applicationNo(appNo)
                .attempt1(s.getAttempt1())
                .attempt2(s.getAttempt2())
                .attempt3(s.getAttempt3())
                .marks1(s.getMarks1())
                .marks2(s.getMarks2())
                .marks3(s.getMarks3())
                .highestDistance(s.getHighestDistance())
                .highestMarks(s.getHighestMarks())
                .build();
    }

    private void addRow(PdfPTable t, String k, String v, Font kFont, Font vFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(k, kFont));
        c1.setBorder(Rectangle.NO_BORDER);
        PdfPCell c2 = new PdfPCell(new Phrase(v == null ? "" : v, vFont));
        c2.setBorder(Rectangle.NO_BORDER);
        t.addCell(c1);
        t.addCell(c2);
    }

    private void addHeader(PdfPTable t, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        t.addCell(cell);
    }

    private void addAttemptRow(PdfPTable t, String label, Float distance, Integer marks, Font font) {
        t.addCell(new PdfPCell(new Phrase(label, font)));
        t.addCell(new PdfPCell(new Phrase(distance != null ? distance.toString() : "", font)));
        t.addCell(new PdfPCell(new Phrase(marks != null ? marks.toString() : "", font)));
    }

    private Image loadCandidatePhoto(String stored) {
        if (stored == null || stored.isBlank()) return null;
        try {
            String data = stored.trim();
            if (data.startsWith("data:")) {
                int comma = data.indexOf(',');
                if (comma > 0) data = data.substring(comma + 1);
            }
            byte[] bytes = Base64.getDecoder().decode(data);
            return Image.getInstance(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}

