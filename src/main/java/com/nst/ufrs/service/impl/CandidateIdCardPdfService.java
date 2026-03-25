package com.nst.ufrs.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Image;
import com.nst.ufrs.domain.Candidate;
import com.nst.ufrs.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CandidateIdCardPdfService {

    private final CandidateRepository candidateRepository;

    private static final DateTimeFormatter DOB_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Transactional(readOnly = true)
    public byte[] generateIdCard(long applicationNo) {
        Candidate candidate = resolveCandidate(applicationNo);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A5, 28, 28, 28, 28);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font labelFont = new Font(Font.HELVETICA, 9, Font.BOLD);
            Font valueFont = new Font(Font.HELVETICA, 9, Font.NORMAL);

            Paragraph t1 = new Paragraph("State Reserve Police Force Group No.07 Daund", titleFont);
            t1.setAlignment(Element.ALIGN_CENTER);
            t1.setSpacingAfter(2f);
            document.add(t1);

            Paragraph t2 = new Paragraph("Armed Police Recruitment Year 2026", titleFont);
            t2.setAlignment(Element.ALIGN_CENTER);
            t2.setSpacingAfter(2f);
            document.add(t2);

            Paragraph t3 = new Paragraph("Photo & Biometrics Registration", titleFont);
            t3.setAlignment(Element.ALIGN_CENTER);
            t3.setSpacingAfter(4f);
            document.add(t3);

            PdfPTable cardTable = new PdfPTable(new float[]{2.5f, 1.4f});
            cardTable.setWidthPercentage(100);

            PdfPTable left = new PdfPTable(2);
            left.setWidthPercentage(100);

            // Heading row
            PdfPCell heading = new PdfPCell(new Phrase("Candidate Details", labelFont));
            heading.setBorder(Rectangle.NO_BORDER);
            heading.setColspan(2);
            heading.setPaddingBottom(4f);
            left.addCell(heading);

            Long appNo = candidate.getApplicationNo();
            Long tokenNo = candidate.getTokenNo();
            String fullName = buildFullName(candidate);
            String gender = safe(candidate.getGender());
            String mobile = candidate.getMobileNo() != null ? candidate.getMobileNo().toString() : "";
            String dobText = candidate.getDob() != null ? candidate.getDob().format(DOB_FMT) : "";
            Integer age = computeAge(candidate.getDob());
            String ageText = age != null ? age.toString() : "";
            String email = safe(candidate.getEmailId());
            String post = safe(candidate.getPost());
            String religion = safe(candidate.getReligion());
            String applicationCategory = safe(candidate.getApplicationCategory());
            String parallelReservation = safe(candidate.getParallelReservation());

            addRow(left, "Application No :", appNo != null ? appNo.toString() : "", labelFont, valueFont);
            addRow(left, "Token No :", tokenNo != null ? tokenNo.toString() : "", labelFont, valueFont);
            addRow(left, "Name :", fullName, labelFont, valueFont);
            addRow(left, "Gender :", gender, labelFont, valueFont);
            addRow(left, "Mobile No :", mobile, labelFont, valueFont);
            addRow(left, "DOB :", dobText, labelFont, valueFont);
            addRow(left, "Age :", ageText, labelFont, valueFont);
            addRow(left, "Email :", email, labelFont, valueFont);
            addRow(left, "Post :", post, labelFont, valueFont);
            addRow(left, "Religion :", religion, labelFont, valueFont);
            addRow(left, "Application Category :", applicationCategory, labelFont, valueFont);
            addRow(left, "Parallel Reservation :", parallelReservation, labelFont, valueFont);

            PdfPCell leftCell = new PdfPCell(left);
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setPadding(4f);
            cardTable.addCell(leftCell);

            // Right side: photo + authorised signature text below
            PdfPTable right = new PdfPTable(1);
            right.setWidthPercentage(100);

            Image photo = loadPhoto(candidate.getPhoto());
            PdfPCell photoCell;
            if (photo != null) {
                photo.scaleToFit(110, 135);
                photoCell = new PdfPCell(photo, true);
            } else {
                photoCell = new PdfPCell(new Phrase("Photo Not Available", valueFont));
                photoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                photoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                photoCell.setFixedHeight(135f);
            }
            photoCell.setBorder(Rectangle.NO_BORDER);
            photoCell.setPaddingBottom(6f);
            right.addCell(photoCell);

            PdfPCell authCell = new PdfPCell(new Phrase("Authorised Officer Signatre", labelFont));
            authCell.setBorder(Rectangle.NO_BORDER);
            authCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            right.addCell(authCell);

            PdfPCell rightOuter = new PdfPCell(right);
            rightOuter.setBorder(Rectangle.NO_BORDER);
            rightOuter.setPadding(4f);
            cardTable.addCell(rightOuter);

            document.add(cardTable);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to generate ID card PDF: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Runtime error while generating ID card PDF", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate ID card PDF", e);
        }
    }

    private Candidate resolveCandidate(long applicationNo) {
        List<Candidate> matches = candidateRepository.findAllByApplicationNoOrderByIdDesc(applicationNo);
        if (matches.isEmpty()) {
            throw new NoSuchElementException("Candidate not found");
        }
        return matches.get(0);
    }

    private static void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setBorder(Rectangle.NO_BORDER);
        PdfPCell c2 = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        c2.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);
        table.addCell(c2);
    }

    private static Integer computeAge(LocalDate dob) {
        if (dob == null) return null;
        return Period.between(dob, LocalDate.now()).getYears();
    }

    private static String buildFullName(Candidate c) {
        StringBuilder sb = new StringBuilder();
        if (c.getFirstName() != null && !c.getFirstName().isBlank()) sb.append(c.getFirstName().trim());
        if (c.getFatherName() != null && !c.getFatherName().isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(c.getFatherName().trim());
        }
        if (c.getSurname() != null && !c.getSurname().isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(c.getSurname().trim());
        }
        return sb.toString();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static Image loadPhoto(String stored) {
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
}

