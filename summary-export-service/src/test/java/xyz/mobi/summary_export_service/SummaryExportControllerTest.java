package xyz.mobi.summary_export_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import xyz.mobi.summary_export_service.controller.SummaryExportController;
import xyz.mobi.summary_export_service.service.SummaryExcelExportService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummaryExportControllerTest {

    @Mock
    private SummaryExcelExportService summaryExcelExportService;

    @InjectMocks
    private SummaryExportController controller;

    @Test
    @DisplayName("Should return 200 OK with proper headers for project summary export")
    void testExportProjectSummaryEndpoint() {
        byte[] mockBytes = new byte[]{1, 2, 3, 4};
        when(summaryExcelExportService.exportProjectSummary(1)).thenReturn(mockBytes);

        ResponseEntity<byte[]> response = controller.exportProjectSummary(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockBytes);
        assertThat(response.getHeaders().getFirst("Content-Disposition"))
                .isEqualTo("attachment; filename=\"QA_Summary_Report_Project_1.xlsx\"");
        assertThat(response.getHeaders().getContentType().toString())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Test
    @DisplayName("Should return 200 OK with proper headers for feature summary export")
    void testExportFeatureSummaryEndpoint() {
        byte[] mockBytes = new byte[]{5, 6, 7, 8};
        when(summaryExcelExportService.exportFeatureSummary(10)).thenReturn(mockBytes);

        ResponseEntity<byte[]> response = controller.exportFeatureSummary(10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockBytes);
        assertThat(response.getHeaders().getFirst("Content-Disposition"))
                .isEqualTo("attachment; filename=\"QA_Summary_Report_Feature_10.xlsx\"");
        assertThat(response.getHeaders().getContentType().toString())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
}
