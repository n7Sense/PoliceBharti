package com.nst.ufrs.service.impl;

import com.nst.ufrs.domain.Candidate;
import com.nst.ufrs.domain.PhysicalTest;
import com.nst.ufrs.dto.PhysicalTestDto;
import com.nst.ufrs.dto.PhysicalTestUpsertRequest;
import com.nst.ufrs.repository.CandidateRepository;
import com.nst.ufrs.repository.PhysicalTestRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.awt.Color;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhysicalTestService {

    private final CandidateRepository candidateRepository;
    private final PhysicalTestRepository physicalTestRepository;

    private static final float MIN_HEIGHT_CM_DEFAULT = 165f;
    private static final float MIN_CHEST_CM = 79f;
    private static final float MIN_EXP_CHEST_CM = 84f;

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

        if (request.getHeight() != null) pt.setHeight(request.getHeight());
        if (request.getChest() != null) pt.setChest(request.getChest());
        if (request.getExpandedChest() != null) pt.setExpandedChest(request.getExpandedChest());
        pt.setRejectReason(normalizeReason(request.getRejectReason()));

        if (request.getHeight1() != null) pt.setHeight1(request.getHeight1());
        if (request.getChest1() != null) pt.setChest1(request.getChest1());
        if (request.getExpandedChest1() != null) pt.setExpandedChest1(request.getExpandedChest1());
        pt.setRejectReason1(normalizeReason(request.getRejectReason1()));

        if (request.getHeight2() != null) pt.setHeight2(request.getHeight2());
        if (request.getChest2() != null) pt.setChest2(request.getChest2());
        if (request.getExpandedChest2() != null) pt.setExpandedChest2(request.getExpandedChest2());
        pt.setRejectReason2(normalizeReason(request.getRejectReason2()));

        applyAutoStatusAndReason(candidate, pt);
        updateCandidateStatusFromPhysicalTest(candidate, pt);

        PhysicalTest saved = physicalTestRepository.save(pt);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public byte[] generatePdfReport(long applicationNo) {
        Candidate candidate = resolveCandidate(applicationNo);
        PhysicalTest pt = physicalTestRepository.findByCandidateId(candidate.getId())
                .orElseThrow(() -> new NoSuchElementException("Physical test not found"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font hFont = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font nFont = new Font(Font.HELVETICA, 11, Font.NORMAL);

            Paragraph title = new Paragraph("Physical Test Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            PdfPTable top = new PdfPTable(new float[]{2f, 1f});
            top.setWidthPercentage(100);

            PdfPTable details = new PdfPTable(new float[]{1f, 2f});
            details.setWidthPercentage(100);

            addRow(details, "Application No", String.valueOf(candidate.getApplicationNo()), hFont, nFont);
            addRow(details, "Name", safe(candidate.getFirstName()) + " " + safe(candidate.getSurname()), hFont, nFont);
            addRow(details, "Post", safe(candidate.getPost()), hFont, nFont);
            addRow(details, "Gender", safe(candidate.getGender()), hFont, nFont);
            addRow(details, "DOB", candidate.getDob() != null ? candidate.getDob().toString() : "", hFont, nFont);
            addRow(details, "Mobile", candidate.getMobileNo() != null ? candidate.getMobileNo().toString() : "", hFont, nFont);
            addRow(details, "Category", safe(candidate.getApplicationCategory()), hFont, nFont);

            PdfPCell detailsCell = new PdfPCell(details);
            detailsCell.setBorder(Rectangle.NO_BORDER);
            top.addCell(detailsCell);

            Image photo = loadCandidatePhoto(candidate.getPhoto());
            PdfPCell imgCell;
            if (photo != null) {
                photo.scaleToFit(140, 180);
                imgCell = new PdfPCell(photo, true);
            } else {
                imgCell = new PdfPCell(new Phrase("No Photo", nFont));
            }
            imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            imgCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            imgCell.setFixedHeight(190);
            top.addCell(imgCell);

            document.add(top);
            document.add(Chunk.NEWLINE);

            Paragraph limits = new Paragraph(
                    String.format("Limits: Height >= %.0f cm, Chest >= %.0f cm, Expanded Chest >= %.0f cm. (SRPF may vary.)",
                            MIN_HEIGHT_CM_DEFAULT, MIN_CHEST_CM, MIN_EXP_CHEST_CM),
                    nFont
            );
            document.add(limits);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(new float[]{1.2f, 1f, 1f, 1.2f, 2.6f});
            table.setWidthPercentage(100);
            addHeader(table, "Attempt", hFont);
            addHeader(table, "Height (cm)", hFont);
            addHeader(table, "Chest (cm)", hFont);
            addHeader(table, "Exp Chest (cm)", hFont);
            addHeader(table, "Result / Reason", hFont);

            // Main
            if (pt.getHeight() != null || pt.getChest() != null || pt.getExpandedChest() != null) {
                addAttemptRow(table, "Main", pt.getHeight(), pt.getChest(), pt.getExpandedChest(), pt.getStatus(), pt.getRejectReason(), nFont);
            }
            // Appeal 1
            if (pt.getHeight1() != null || pt.getChest1() != null || pt.getExpandedChest1() != null) {
                addAttemptRow(table, "Appeal 1", pt.getHeight1(), pt.getChest1(), pt.getExpandedChest1(), pt.getStatus1(), pt.getRejectReason1(), nFont);
            }
            // Appeal 2
            if (pt.getHeight2() != null || pt.getChest2() != null || pt.getExpandedChest2() != null) {
                addAttemptRow(table, "Appeal 2", pt.getHeight2(), pt.getChest2(), pt.getExpandedChest2(), pt.getStatus2(), pt.getRejectReason2(), nFont);
            }

            document.add(table);
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

    private void applyAutoStatusAndReason(Candidate candidate, PhysicalTest pt) {
        float minHeight = MIN_HEIGHT_CM_DEFAULT;
        if (candidate != null && candidate.getPost() != null) {
            String post = candidate.getPost().toLowerCase();
            if (post.contains("srpf")) {
                // placeholder for post-specific tweaks
                minHeight = MIN_HEIGHT_CM_DEFAULT;
            }
        }

        // Main
        if (pt.getHeight() != null || pt.getChest() != null || pt.getExpandedChest() != null) {
            var main = evaluate(pt.getHeight(), pt.getChest(), pt.getExpandedChest(), minHeight);
            pt.setStatus(main.pass);
            if (pt.getRejectReason() == null || pt.getRejectReason().isBlank()) pt.setRejectReason(main.reason);
        }

        // Appeal 1
        if (pt.getHeight1() != null || pt.getChest1() != null || pt.getExpandedChest1() != null) {
            var a1 = evaluate(pt.getHeight1(), pt.getChest1(), pt.getExpandedChest1(), minHeight);
            pt.setStatus1(a1.pass);
            if (pt.getRejectReason1() == null || pt.getRejectReason1().isBlank()) pt.setRejectReason1(a1.reason);
        }

        // Appeal 2
        if (pt.getHeight2() != null || pt.getChest2() != null || pt.getExpandedChest2() != null) {
            var a2 = evaluate(pt.getHeight2(), pt.getChest2(), pt.getExpandedChest2(), minHeight);
            pt.setStatus2(a2.pass);
            if (pt.getRejectReason2() == null || pt.getRejectReason2().isBlank()) pt.setRejectReason2(a2.reason);
        }
    }

    private record Eval(boolean pass, String reason) {}

    private Eval evaluate(Float height, Float chest, Float expChest, float minHeight) {
        StringBuilder reason = new StringBuilder();
        boolean ok = true;

        if (height == null) {
            ok = false;
            reason.append("Height missing. ");
        } else if (height < minHeight) {
            ok = false;
            reason.append("Height below ").append((int) minHeight).append(" cm. ");
        }

        if (chest == null) {
            ok = false;
            reason.append("Chest missing. ");
        } else if (chest < MIN_CHEST_CM) {
            ok = false;
            reason.append("Chest below ").append((int) MIN_CHEST_CM).append(" cm. ");
        }

        if (expChest == null) {
            ok = false;
            reason.append("Expanded chest missing. ");
        } else if (expChest < MIN_EXP_CHEST_CM) {
            ok = false;
            reason.append("Expanded chest below ").append((int) MIN_EXP_CHEST_CM).append(" cm. ");
        }

        return new Eval(ok, ok ? "PASS" : reason.toString().trim());
    }

    private void updateCandidateStatusFromPhysicalTest(Candidate candidate, PhysicalTest pt) {
        if (candidate == null || pt == null) return;

        Boolean derived = deriveFinalAttemptStatus(pt);
        if (derived == null) return;

        candidate.setPhysicalTestStatus(derived);
        candidateRepository.save(candidate);
    }

    /**
     * Derive "final" physical test status based on the latest attempt
     * that has been entered (Appeal 2 > Appeal 1 > Main).
     */
    private Boolean deriveFinalAttemptStatus(PhysicalTest pt) {
        if (pt.getHeight2() != null || pt.getChest2() != null || pt.getExpandedChest2() != null) {
            return pt.getStatus2();
        }
        if (pt.getHeight1() != null || pt.getChest1() != null || pt.getExpandedChest1() != null) {
            return pt.getStatus1();
        }
        if (pt.getHeight() != null || pt.getChest() != null || pt.getExpandedChest() != null) {
            return pt.getStatus();
        }
        return null;
    }

    private String normalizeReason(String r) {
        if (r == null) return null;
        String t = r.trim();
        return t.isEmpty() ? null : t;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
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
        cell.setBackgroundColor(new Color(220, 220, 220));
        cell.setPadding(6);
        t.addCell(cell);
    }

    private void addAttemptRow(PdfPTable t, String label, Float h, Float c, Float e, Boolean status, String reason, Font font) {
        t.addCell(new PdfPCell(new Phrase(label, font)));
        t.addCell(new PdfPCell(new Phrase(h == null ? "" : String.valueOf(h), font)));
        t.addCell(new PdfPCell(new Phrase(c == null ? "" : String.valueOf(c), font)));
        t.addCell(new PdfPCell(new Phrase(e == null ? "" : String.valueOf(e), font)));

        String res = status == null ? "" : (status ? "PASS" : "REJECT");
        String rr = reason == null ? "" : reason;
        t.addCell(new PdfPCell(new Phrase((rr.isBlank() ? res : (res + " - " + rr)), font)));
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
                .rejectReason(pt.getRejectReason())
                .height1(pt.getHeight1())
                .chest1(pt.getChest1())
                .expandedChest1(pt.getExpandedChest1())
                .status1(pt.getStatus1())
                .rejectReason1(pt.getRejectReason1())
                .height2(pt.getHeight2())
                .chest2(pt.getChest2())
                .expandedChest2(pt.getExpandedChest2())
                .status2(pt.getStatus2())
                .rejectReason2(pt.getRejectReason2())
                .build();
    }
}

