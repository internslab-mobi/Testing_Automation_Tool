package xyz.mobi.testingautomationtool.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.mobi.testingautomationtool.exception.ExcelProcessingException;

import static org.junit.jupiter.api.Assertions.*;

class SummaryExportClientTest {

    private SummaryExportClient client;

    @BeforeEach
    void setUp() {
        // Points to an unused local port to test failure handling
        client = new SummaryExportClient("http://localhost:59999");
    }

    @Test
    void testConnectionFailureThrowsExcelProcessingException() {
        assertThrows(ExcelProcessingException.class, () -> {
            client.exportProjectSummary(999);
        });
    }

    @Test
    void testFeatureExportConnectionFailureThrowsExcelProcessingException() {
        assertThrows(ExcelProcessingException.class, () -> {
            client.exportFeatureSummary(999);
        });
    }

    @Test
    void testTemplateDownloadConnectionFailureThrowsExcelProcessingException() {
        assertThrows(ExcelProcessingException.class, () -> {
            client.downloadTestCaseTemplate(1, 1);
        });
    }
}
