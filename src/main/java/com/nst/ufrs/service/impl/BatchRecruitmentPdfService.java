package com.nst.ufrs.service.impl;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.nst.ufrs.dto.BatchCandidateRowDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BatchRecruitmentPdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd / MM / yyyy");

    public byte[] generateRecruitmentDocument(String batchCode, LocalDate date, List<BatchCandidateRowDto> candidates) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 48, 48, 48, 48);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font h1 = new Font(Font.HELVETICA, 19, Font.BOLD);
            Font h2 = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font h3 = new Font(Font.HELVETICA, 14, Font.NORMAL);
            Font label = new Font(Font.HELVETICA, 12, Font.NORMAL);
            Font th = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font td = new Font(Font.HELVETICA, 11, Font.NORMAL);

            Paragraph p1 = new Paragraph("State Reserve Police Force Group No.7 Daund", h1);
            p1.setAlignment(Element.ALIGN_CENTER);
            document.add(p1);

            Paragraph p2 = new Paragraph("Armed Police Recruitment Year 2026", h2);
            p2.setAlignment(Element.ALIGN_CENTER);
            p2.setSpacingBefore(4);
            document.add(p2);

            String code = (batchCode == null || batchCode.isBlank()) ? "____" : batchCode.trim();
            Paragraph p3 = new Paragraph("Batch Code - " + code, h3);
            p3.setAlignment(Element.ALIGN_CENTER);
            p3.setSpacingBefore(6);
            p3.setSpacingAfter(10);
            document.add(p3);

            String dateText = "Date: " + (date != null ? DATE_FMT.format(date) : "_ / _ / 20__");
            Paragraph dateP = new Paragraph(dateText, label);
            dateP.setAlignment(Element.ALIGN_LEFT);
            dateP.setSpacingAfter(12);
            document.add(dateP);

            PdfPTable table = new PdfPTable(new float[]{0.7f, 1.0f, 1.4f, 2.6f, 1.5f});
            table.setWidthPercentage(100);

            addHeaderCell(table, "Sr.No", th);
            addHeaderCell(table, "Running No", th);
            addHeaderCell(table, "Application No", th);
            addHeaderCell(table, "Name", th);
            addHeaderCell(table, "Mobile No", th);

            if (candidates == null || candidates.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("No candidates found for this batch.", td));
                empty.setColspan(5);
                empty.setPadding(10);
                empty.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(empty);
            } else {
                int i = 1;
                for (BatchCandidateRowDto c : candidates) {
                    table.addCell(bodyCell(String.valueOf(i++), td, Element.ALIGN_CENTER));
                    table.addCell(bodyCell(c.getRunningNumber() != null ? String.valueOf(c.getRunningNumber()) : "", td, Element.ALIGN_CENTER));
                    table.addCell(bodyCell(c.getApplicationNo() != null ? String.valueOf(c.getApplicationNo()) : "", td, Element.ALIGN_LEFT));
                    table.addCell(bodyCell(safe(c.getName()), td, Element.ALIGN_LEFT));
                    table.addCell(bodyCell(c.getMobileNo() != null ? String.valueOf(c.getMobileNo()) : "", td, Element.ALIGN_LEFT));
                }
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private static void addHeaderCell(PdfPTable t, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setBorderWidth(1f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(cell);
    }

    private static PdfPCell bodyCell(String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setPadding(6);
        cell.setBorderWidth(1f);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setNoWrap(false);
        cell.setBorder(Rectangle.BOX);
        return cell;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}

