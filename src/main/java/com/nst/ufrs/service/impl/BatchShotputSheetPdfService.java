package com.nst.ufrs.service.impl;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
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
public class BatchShotputSheetPdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd / MM / yyyy");

    /**
     * Generate a single–page A4 shotput sheet for a batch.
     * Always renders 20 rows (Sr.No 1–20). For available candidates,
     * Running No and Name are filled from data; all other fields remain blank.
     */
    public byte[] generateShotputSheet(String batchCode, LocalDate date, List<BatchCandidateRowDto> candidates) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 32, 32, 32, 32);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            Font h1 = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font h2 = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font h3 = new Font(Font.HELVETICA, 12, Font.NORMAL);
            Font label = new Font(Font.HELVETICA, 9, Font.NORMAL);
            Font th = new Font(Font.HELVETICA, 8, Font.BOLD);
            Font td = new Font(Font.HELVETICA, 8, Font.NORMAL);

            Paragraph p1 = new Paragraph("State Reserve Police Force Group No.7 Daund", h1);
            p1.setAlignment(Element.ALIGN_CENTER);
            document.add(p1);

            Paragraph p2 = new Paragraph("Armed Police Recruitment Year 2026", h2);
            p2.setAlignment(Element.ALIGN_CENTER);
            p2.setSpacingBefore(3);
            document.add(p2);

            String code = (batchCode == null || batchCode.isBlank()) ? "____" : batchCode.trim();
            Paragraph p3 = new Paragraph("Shotput Sheet - Batch Code " + code, h3);
            p3.setAlignment(Element.ALIGN_CENTER);
            p3.setSpacingBefore(4);
            p3.setSpacingAfter(8);
            document.add(p3);

            String dateText = "Date: " + (date != null ? DATE_FMT.format(date) : "_ / _ / 20__");
            Paragraph dateP = new Paragraph(dateText, label);
            dateP.setAlignment(Element.ALIGN_LEFT);
            dateP.setSpacingAfter(6);
            document.add(dateP);

            float usableWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();

            // Table layout tuned to keep everything on a single A4 page.
            PdfPTable table = new PdfPTable(new float[]{
                    0.6f,  // Sr.No
                    0.9f,  // Running No
                    2.2f,  // Name
                    0.9f,  // Attempt 1
                    0.9f,  // Attempt 2
                    0.9f,  // Attempt 3
                    0.9f,  // Marks 1
                    0.9f,  // Marks 2
                    0.9f,  // Marks 3
                    1.2f   // Remark
            });
            table.setWidthPercentage(100);

            int maxRows = 20;
            int filled = candidates != null ? Math.min(maxRows, candidates.size()) : 0;

            PdfPTable footer = new PdfPTable(new float[]{1f, 1f, 1f});
            footer.setTotalWidth(usableWidth);
            footer.setLockedWidth(true);

            PdfPCell left = new PdfPCell();
            left.setBorder(Rectangle.NO_BORDER);
            left.setHorizontalAlignment(Element.ALIGN_LEFT);
            left.addElement(new Phrase("Head of Detail", td));
            left.addElement(new Phrase("Sign : ......................", td));
            left.addElement(new Phrase("Name : ......................", td));
            left.addElement(new Phrase("Designation : ......................", td));
            footer.addCell(left);

            PdfPCell middle = new PdfPCell();
            middle.setBorder(Rectangle.NO_BORDER);
            middle.setHorizontalAlignment(Element.ALIGN_CENTER);
            middle.addElement(new Phrase("Table Chief Officer", td));
            middle.addElement(new Phrase("Sign : ......................", td));
            middle.addElement(new Phrase("Name : ......................", td));
            middle.addElement(new Phrase("Designation : ......................", td));
            footer.addCell(middle);

            PdfPCell right = new PdfPCell();
            right.setBorder(Rectangle.NO_BORDER);
            right.setHorizontalAlignment(Element.ALIGN_RIGHT);
            right.addElement(new Phrase("External Class Chief Officer", td));
            right.addElement(new Phrase("Sign : ......................", td));
            right.addElement(new Phrase("Name : ......................", td));
            right.addElement(new Phrase("Designation : ......................", td));
            footer.addCell(right);

            footer.completeRow();
            footer.calculateHeights(true);
            float footerHeight = footer.getTotalHeight();

            // Compute the available height between header end and footer start,
            // then distribute it across the table header + 20 rows.
            float headerBottomY = writer.getVerticalPosition(true);
            float footerTopY = document.bottomMargin() + footerHeight;
            float gap = 6f;
            float availableTableHeight = Math.max(120f, headerBottomY - (footerTopY + gap));
            float perRowHeight = availableTableHeight / (maxRows + 1f); // +1 for table header row

            addHeaderCell(table, "Sr.No", th, perRowHeight);
            addHeaderCell(table, "Running No", th, perRowHeight);
            addHeaderCell(table, "Name", th, perRowHeight);
            addHeaderCell(table, "Attempt 1", th, perRowHeight);
            addHeaderCell(table, "Attempt 2", th, perRowHeight);
            addHeaderCell(table, "Attempt 3", th, perRowHeight);
            addHeaderCell(table, "Marks 1", th, perRowHeight);
            addHeaderCell(table, "Marks 2", th, perRowHeight);
            addHeaderCell(table, "Marks 3", th, perRowHeight);
            addHeaderCell(table, "Remark", th, perRowHeight);

            for (int i = 0; i < maxRows; i++) {
                BatchCandidateRowDto row = (i < filled ? candidates.get(i) : null);
                String runningNo = "";
                String name = "";
                if (row != null) {
                    if (row.getRunningNumber() != null) {
                        runningNo = String.valueOf(row.getRunningNumber());
                    }
                    name = safe(row.getName());
                }

                table.addCell(bodyCell(String.valueOf(i + 1), td, Element.ALIGN_CENTER, perRowHeight));
                table.addCell(bodyCell(runningNo, td, Element.ALIGN_CENTER, perRowHeight));
                table.addCell(bodyCell(name, td, Element.ALIGN_LEFT, perRowHeight));
                table.addCell(bodyCell("", td, Element.ALIGN_CENTER, perRowHeight));
                table.addCell(bodyCell("", td, Element.ALIGN_CENTER, perRowHeight));
                table.addCell(bodyCell("", td, Element.ALIGN_CENTER, perRowHeight));
                table.addCell(bodyCell("", td, Element.ALIGN_CENTER, perRowHeight));
                table.addCell(bodyCell("", td, Element.ALIGN_CENTER, perRowHeight));
                table.addCell(bodyCell("", td, Element.ALIGN_CENTER, perRowHeight));
                table.addCell(bodyCell("", td, Element.ALIGN_LEFT, perRowHeight));
            }

            table.setTotalWidth(usableWidth);
            table.setLockedWidth(true);

            PdfContentByte cb = writer.getDirectContent();
            float leftX = document.leftMargin();

            // Draw footer fixed at the bottom.
            footer.writeSelectedRows(0, -1, leftX, footerTopY, cb);

            // Draw table to fill the space above footer.
            float tableTopY = headerBottomY - 2f;
            table.writeSelectedRows(0, -1, leftX, tableTopY, cb);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate Shotput sheet PDF: " + e.getMessage(), e);
        }
    }

    private static void addHeaderCell(PdfPTable t, String text, Font font, float fixedHeight) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(2);
        cell.setBorderWidth(0.7f);
        cell.setFixedHeight(fixedHeight);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(cell);
    }

    private static PdfPCell bodyCell(String text, Font font, int align, float fixedHeight) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setPadding(2);
        cell.setBorderWidth(0.6f);
        cell.setFixedHeight(fixedHeight);
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

