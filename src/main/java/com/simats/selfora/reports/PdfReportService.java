package com.simats.selfora.reports;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.simats.selfora.child.ChildService;
import com.simats.selfora.dto.ProgressDtos;
import com.simats.selfora.entity.Child;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.progress.ProgressAnalyticsService;
import com.simats.selfora.repository.ChildRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service producing authentic, formatted PDF clinical ADL reports for pediatric occupational therapy.
 */
@Service
public class PdfReportService {

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private ChildService childService;

    @Autowired
    private ProgressAnalyticsService progressAnalyticsService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public byte[] generateChildReportPdf(Long childId, Long activityId) {
        // Enforce child authorization access
        childService.validateChildAccessForCurrentUser(childId);

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found with ID: " + childId));

        Long targetActivityId = activityId != null ? activityId : 1L;
        ProgressDtos.ProgressSummaryResponse progress = progressAnalyticsService.getProgressSummary(childId, targetActivityId);

        // Fetch assigned therapist and linked caregiver names
        String therapistName = "Unassigned";
        try {
            List<Map<String, Object>> tList = jdbcTemplate.queryForList(
                    "SELECT u.full_name FROM users u JOIN therapist_children tc ON u.id = tc.therapist_id WHERE tc.child_id = ?",
                    childId
            );
            if (!tList.isEmpty()) {
                therapistName = (String) tList.get(0).get("full_name");
            }
        } catch (Exception ignored) {}

        String caregiverName = "Unassigned";
        try {
            List<Map<String, Object>> cList = jdbcTemplate.queryForList(
                    "SELECT u.full_name FROM users u JOIN caregiver_children cc ON u.id = cc.caregiver_id WHERE cc.child_id = ?",
                    childId
            );
            if (!cList.isEmpty()) {
                caregiverName = (String) cList.get(0).get("full_name");
            }
        } catch (Exception ignored) {}

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Color Palette
            Color primaryBlue = new Color(37, 99, 235);
            Color darkSlate = new Color(15, 23, 42);
            Color textSecondary = new Color(100, 116, 139);
            Color lightBg = new Color(248, 250, 252);
            Color borderGray = new Color(226, 232, 240);
            Color successGreen = new Color(22, 163, 74);

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, primaryBlue);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, textSecondary);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, darkSlate);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, textSecondary);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, darkSlate);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font tableCellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, darkSlate);
            Font tableCellBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, darkSlate);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, textSecondary);

            // Title Banner
            Paragraph title = new Paragraph("SELFORA CLINICAL ADL EVALUATION REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Pediatric Occupational Therapy Self-Dressing & Task Breakdown Protocol", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            // Generation Timestamp
            String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss"));
            Paragraph genDate = new Paragraph("Report Generated: " + formattedDate, footerFont);
            genDate.setAlignment(Element.ALIGN_RIGHT);
            genDate.setSpacingAfter(10);
            document.add(genDate);

            // Patient Information Table
            PdfPTable patientTable = new PdfPTable(4);
            patientTable.setWidthPercentage(100);
            patientTable.setWidths(new float[]{25, 25, 25, 25});
            patientTable.setSpacingAfter(15);

            addPatientCell(patientTable, "CHILD FULL NAME", child.getFirstName() + " " + (child.getLastName() != null ? child.getLastName() : ""), labelFont, valueFont, lightBg, borderGray);
            addPatientCell(patientTable, "CHILD ID", "#" + child.getId(), labelFont, valueFont, lightBg, borderGray);
            addPatientCell(patientTable, "DATE OF BIRTH", child.getDateOfBirth() != null ? child.getDateOfBirth().toString() : "Not Recorded", labelFont, valueFont, lightBg, borderGray);
            addPatientCell(patientTable, "GENDER", child.getGender() != null ? child.getGender() : "MALE", labelFont, valueFont, lightBg, borderGray);

            addPatientCell(patientTable, "PRIMARY THERAPIST", therapistName, labelFont, valueFont, lightBg, borderGray);
            addPatientCell(patientTable, "LINKED CAREGIVER", caregiverName, labelFont, valueFont, lightBg, borderGray);
            addPatientCell(patientTable, "ACTIVE ADL TARGET", progress.getActivityTitle() != null ? progress.getActivityTitle() : "ADL Training", labelFont, valueFont, lightBg, borderGray);
            addPatientCell(patientTable, "SESSIONS COMPLETED", String.valueOf(progress.getTotalSessionsCompleted()), labelFont, valueFont, lightBg, borderGray);

            document.add(patientTable);

            // Clinical Diagnosis & Notes
            if (child.getDiagnosisNotes() != null && !child.getDiagnosisNotes().trim().isEmpty()) {
                Paragraph diagHeader = new Paragraph("CLINICAL DIAGNOSIS & REHABILITATION FOCUS", sectionFont);
                diagHeader.setSpacingAfter(4);
                document.add(diagHeader);

                Paragraph diagBody = new Paragraph(child.getDiagnosisNotes(), valueFont);
                diagBody.setSpacingAfter(15);
                document.add(diagBody);
            }

            // Executive Summary KPIs
            Paragraph kpiHeader = new Paragraph("CLINICAL PERFORMANCE & INDEPENDENCE SUMMARY", sectionFont);
            kpiHeader.setSpacingAfter(8);
            document.add(kpiHeader);

            PdfPTable kpiTable = new PdfPTable(3);
            kpiTable.setWidthPercentage(100);
            kpiTable.setWidths(new float[]{33, 33, 34});
            kpiTable.setSpacingAfter(15);

            addKpiCell(kpiTable, "Overall Independence", progress.getOverallIndependencePercentage() + "%", primaryBlue, lightBg, borderGray);
            addKpiCell(kpiTable, "Total Clinical Trials", String.valueOf(progress.getTotalSessionsCompleted()), darkSlate, lightBg, borderGray);
            addKpiCell(kpiTable, "Clinical Status", progress.getOverallIndependencePercentage() >= 80 ? "MASTERY ACHIEVED" : "ACTIVE TRAINING", successGreen, lightBg, borderGray);

            document.add(kpiTable);

            // Step-by-Step Task Breakdown Table
            Paragraph stepHeader = new Paragraph("TASK STEP BREAKDOWN & PROMPT HIERARCHY ANALYSIS", sectionFont);
            stepHeader.setSpacingAfter(8);
            document.add(stepHeader);

            PdfPTable stepTable = new PdfPTable(5);
            stepTable.setWidthPercentage(100);
            stepTable.setWidths(new float[]{10, 45, 18, 15, 12});
            stepTable.setSpacingAfter(15);

            // Table Header
            String[] headers = {"Step #", "Clinical Task Step Description", "Prompt Level", "Independence", "Status"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(primaryBlue);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                stepTable.addCell(cell);
            }

            // Populate rows
            if (progress.getStepProgress() != null && !progress.getStepProgress().isEmpty()) {
                int rowIdx = 0;
                for (ProgressDtos.StepProgressItem s : progress.getStepProgress()) {
                    Color rowBg = (rowIdx++ % 2 == 0) ? Color.WHITE : lightBg;

                    addStepTableCell(stepTable, String.valueOf(s.getStepNumber()), tableCellBold, rowBg, Element.ALIGN_CENTER);
                    addStepTableCell(stepTable, s.getStepTitle() != null ? s.getStepTitle() : ("Step " + s.getStepNumber()), tableCellFont, rowBg, Element.ALIGN_LEFT);
                    addStepTableCell(stepTable, s.getCurrentPromptLevel() != null ? s.getCurrentPromptLevel() : "Independent", tableCellFont, rowBg, Element.ALIGN_CENTER);
                    addStepTableCell(stepTable, s.getIndependencePercentage() + "%", tableCellBold, rowBg, Element.ALIGN_CENTER);
                    addStepTableCell(stepTable, s.getIndependencePercentage() >= 80 ? "Mastered" : "In Progress", tableCellFont, rowBg, Element.ALIGN_CENTER);
                }
            } else {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No clinical assessment step data recorded yet.", tableCellFont));
                emptyCell.setColspan(5);
                emptyCell.setPadding(10);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                stepTable.addCell(emptyCell);
            }

            document.add(stepTable);

            // Environmental Generalization
            if (progress.getGeneralizationProgress() != null && !progress.getGeneralizationProgress().isEmpty()) {
                Paragraph genHeader = new Paragraph("ENVIRONMENTAL GENERALIZATION PROGRESS", sectionFont);
                genHeader.setSpacingAfter(8);
                document.add(genHeader);

                PdfPTable genTable = new PdfPTable(3);
                genTable.setWidthPercentage(100);
                genTable.setWidths(new float[]{40, 30, 30});
                genTable.setSpacingAfter(15);

                for (String gh : new String[]{"Environment", "Independence %", "Dominant Prompt Level"}) {
                    PdfPCell cell = new PdfPCell(new Phrase(gh, tableHeaderFont));
                    cell.setBackgroundColor(new Color(71, 85, 105));
                    cell.setPadding(6);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    genTable.addCell(cell);
                }

                int gRow = 0;
                for (ProgressDtos.EnvironmentProgressItem g : progress.getGeneralizationProgress()) {
                    Color rowBg = (gRow++ % 2 == 0) ? Color.WHITE : lightBg;
                    addStepTableCell(genTable, g.getEnvironment() != null ? g.getEnvironment() : "Standard", tableCellFont, rowBg, Element.ALIGN_LEFT);
                    addStepTableCell(genTable, g.getIndependencePercentage() + "%", tableCellBold, rowBg, Element.ALIGN_CENTER);
                    addStepTableCell(genTable, g.getDominantPromptLevel() != null ? g.getDominantPromptLevel() : "Independent", tableCellFont, rowBg, Element.ALIGN_CENTER);
                }

                document.add(genTable);
            }

            // Legal & Clinical Sign-off
            Paragraph signoff = new Paragraph(
                    "\n\nTherapist Clinical Verification & Signature: _________________________________    Date: _________________\n" +
                    "Confidential Medical Record — Developed under SIMATS Pediatric Occupational Therapy Protocol",
                    footerFont
            );
            signoff.setAlignment(Element.ALIGN_CENTER);
            document.add(signoff);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating clinical PDF report: " + e.getMessage(), e);
        }
    }

    private void addPatientCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont, Color bg, Color border) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setBorderColor(border);
        cell.setPadding(6);

        Paragraph p = new Paragraph(label + "\n", labelFont);
        p.add(new Phrase(value, valueFont));
        cell.addElement(p);
        table.addCell(cell);
    }

    private void addKpiCell(PdfPTable table, String label, String value, Color valueColor, Color bg, Color border) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setBorderColor(border);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Font valFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, valueColor);
        Font lblFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(100, 116, 139));

        Paragraph p = new Paragraph(value + "\n", valFont);
        p.setAlignment(Element.ALIGN_CENTER);
        p.add(new Phrase(label, lblFont));
        cell.addElement(p);
        table.addCell(cell);
    }

    private void addStepTableCell(PdfPTable table, String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }
}
