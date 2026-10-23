package xyz.mobi.summary_export_service.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import xyz.mobi.summary_export_service.service.ExcelTemplateService;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'TESTER')")
public class TemplateExportController {

    private final ExcelTemplateService excelTemplateService;

    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @GetMapping({
            "/testcases/template/{projectId}/{featureId}",
            "/export/template/{projectId}/{featureId}"
    })
    public ResponseEntity<byte[]> downloadTemplate(
            @PathVariable Integer projectId,
            @PathVariable Integer featureId) {
        log.info("REST request to generate Excel template for Project ID: {}, Feature ID: {}", projectId, featureId);

        byte[] excelBytes = excelTemplateService.generateTemplate(projectId, featureId);
        String filename = "testcase_template_" + featureId + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .body(excelBytes);
    }

    @GetMapping("/template")
    public void downloadTemplate(
            HttpServletResponse response) throws IOException {

        response.setContentType(
                "application/vnd.openxmlformats-officedocument"
                        + ".spreadsheetml.sheet"
        );

        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"TestCase_Template.xlsx\""
        );

        excelTemplateService.generateTemplate(
                response.getOutputStream()
        );
    }


}
