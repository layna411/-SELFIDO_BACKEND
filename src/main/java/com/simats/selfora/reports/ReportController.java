package com.simats.selfora.reports;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private PdfReportService pdfReportService;

    @GetMapping("/children/{childId}/pdf")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<byte[]> downloadChildReport(
            @PathVariable Long childId,
            @RequestParam(defaultValue = "1") Long activityId) {
        byte[] pdfData = pdfReportService.generateChildReportPdf(childId, activityId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Selfora_Clinical_Report_Child_" + childId + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(pdfData);
    }
}
