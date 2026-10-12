package xyz.mobi.summary_export_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.mobi.summary_export_service.service.SummaryExcelExportService;

@Slf4j
@RestController
@RequestMapping("/export/summary")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'TESTER')")
public class SummaryExportController {

    private final SummaryExcelExportService summaryExcelExportService;

    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @GetMapping("/project/{projectId}")
    public ResponseEntity<byte[]> exportProjectSummary(@PathVariable Integer projectId) {
        log.info("REST request to export summary report for Project ID: {}", projectId);
        byte[] excelBytes = summaryExcelExportService.exportProjectSummary(projectId);

        String filename = "QA_Summary_Report_Project_" + projectId + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .body(excelBytes);
    }

    @GetMapping("/feature/{featureId}")
    public ResponseEntity<byte[]> exportFeatureSummary(@PathVariable Integer featureId) {
        log.info("REST request to export summary report for Feature ID: {}", featureId);
        byte[] excelBytes = summaryExcelExportService.exportFeatureSummary(featureId);

        String filename = "QA_Summary_Report_Feature_" + featureId + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .body(excelBytes);
    }
}
