package xyz.mobi.summary_export_service;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.mobi.summary_export_service.entity.*;
import xyz.mobi.summary_export_service.exception.ResourceNotFoundException;
import xyz.mobi.summary_export_service.repository.*;
import xyz.mobi.summary_export_service.service.impl.SummaryExcelExportServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummaryExcelExportServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private TestCaseRepository testCaseRepository;

    @Mock
    private TestingExecutionRepository testingExecutionRepository;

    @Mock
    private BugRepository bugRepository;

    @InjectMocks
    private SummaryExcelExportServiceImpl exportService;

    private Project sampleProject;
    private Feature sampleFeature;
    private TestCase sampleTestCase1;
    private TestCase sampleTestCase2;
    private TestingExecution sampleExecution1;
    private TestingExecution sampleExecution2;
    private Bug sampleBug;

    @BeforeEach
    void setUp() {
        sampleProject = Project.builder()
                .projectId(1)
                .projectName("OmniChannel E-Commerce Platform")
                .description("Core multi-region shopping and checkout portal")
                .region("NORTH_AMERICA")
                .status("ACTIVE")
                .isDeleted(false)
                .build();

        sampleFeature = Feature.builder()
                .featureId(10)
                .projectId(1)
                .featureName("User Authentication & SSO")
                .status("ACTIVE")
                .isDeleted(false)
                .build();

        sampleTestCase1 = TestCase.builder()
                .testcaseId(101)
                .testcaseFormatId("TC-AUTH-101")
                .featureId(10)
                .title("Verify login with valid credentials")
                .testType("FUNCTIONAL")
                .testPriority("CRITICAL")
                .testcaseStatus("PASS")
                .isDeleted(false)
                .build();

        sampleTestCase2 = TestCase.builder()
                .testcaseId(102)
                .testcaseFormatId("TC-AUTH-102")
                .featureId(10)
                .title("Verify password reset token expiration")
                .testType("SECURITY")
                .testPriority("HIGH")
                .testcaseStatus("FAIL")
                .isDeleted(false)
                .build();

        sampleExecution1 = TestingExecution.builder()
                .executionId(1)
                .testcaseId(101)
                .executionStatus("PASS")
                .automationFeasibility("YES")
                .uiValidations("OAuth redirect confirmed")
                .dbValidations("Token stored in auth_session")
                .executedAt(java.time.Instant.now().minus(java.time.Duration.ofHours(2)))
                .build();

        sampleExecution2 = TestingExecution.builder()
                .executionId(2)
                .testcaseId(102)
                .executionStatus("FAIL")
                .automationFeasibility("PLANNED")
                .uiValidations("Error toast displayed")
                .dbValidations("Expired session not invalidated")
                .executedAt(java.time.Instant.now().minus(java.time.Duration.ofHours(1)))
                .build();

        sampleBug = Bug.builder()
                .bugId(501)
                .feature(sampleFeature)
                .bugFormatId("BUG-USER_AUTH-001")
                .title("Expired JWT tokens still permit read requests")
                .severity(xyz.mobi.summary_export_service.enums.BugSeverity.CRITICAL)
                .priority(xyz.mobi.summary_export_service.enums.BugPriority.CRITICAL)
                .status(xyz.mobi.summary_export_service.enums.BugStatus.OPEN)
                .comments("Filter chain bypass due to missing timestamp validation")
                .isDeleted(false)
                .build();
    }

    @Test
    @DisplayName("Should successfully generate comprehensive multi-sheet Excel report for a project")
    void testExportProjectSummary_Success() throws IOException {
        when(projectRepository.findByProjectIdAndIsDeletedFalse(1)).thenReturn(Optional.of(sampleProject));
        when(featureRepository.findByProjectIdAndIsDeletedFalse(1)).thenReturn(List.of(sampleFeature));
        when(testCaseRepository.findByFeatureIdInAndIsDeletedFalse(any())).thenReturn(List.of(sampleTestCase1, sampleTestCase2));
        when(testingExecutionRepository.findByTestcaseIdIn(any())).thenReturn(List.of(sampleExecution1, sampleExecution2));
        when(bugRepository.findByFeatureIdInAndIsDeletedFalse(any())).thenReturn(List.of(sampleBug));

        byte[] excelBytes = exportService.exportProjectSummary(1);

        assertThat(excelBytes).isNotNull();
        assertThat(excelBytes.length).isGreaterThan(1000);

        // Verify generated Excel content using POI
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(3);
            assertThat(workbook.getSheetName(0)).isEqualTo("Executive_Summary");
            assertThat(workbook.getSheetName(1)).isEqualTo("Testcase_Execution_Summary");
            assertThat(workbook.getSheetName(2)).isEqualTo("Defect_RCA_Breakdown");

            // Verify Executive Summary Sheet
            var execSheet = workbook.getSheet("Executive_Summary");
            assertThat(execSheet).isNotNull();
            String title = execSheet.getRow(0).getCell(0).getStringCellValue();
            assertThat(title).contains("QA EXECUTIVE SUMMARY REPORT");

            // Verify Testcase Execution Summary Sheet
            var tcSheet = workbook.getSheet("Testcase_Execution_Summary");
            assertThat(tcSheet).isNotNull();
            assertThat(tcSheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("TC-AUTH-101");
            assertThat(tcSheet.getRow(2).getCell(12).getStringCellValue()).isEqualTo("PASS");
            assertThat(tcSheet.getRow(3).getCell(0).getStringCellValue()).isEqualTo("TC-AUTH-102");
            assertThat(tcSheet.getRow(3).getCell(12).getStringCellValue()).isEqualTo("FAIL");

            // Verify Defect RCA Breakdown Sheet
            var bugSheet = workbook.getSheet("Defect_RCA_Breakdown");
            assertThat(bugSheet).isNotNull();
            assertThat(bugSheet.getRow(2).getCell(1).getStringCellValue()).isEqualTo("BUG-USER_AUTH-001");
            assertThat(bugSheet.getRow(2).getCell(4).getStringCellValue()).isEqualTo("CRITICAL");
            assertThat(bugSheet.getRow(2).getCell(6).getStringCellValue()).isEqualTo("OPEN");
        }
    }

    @Test
    @DisplayName("Should successfully generate report for a single feature")
    void testExportFeatureSummary_Success() throws IOException {
        when(featureRepository.findByFeatureIdAndIsDeletedFalse(10)).thenReturn(Optional.of(sampleFeature));
        when(projectRepository.findById(1)).thenReturn(Optional.of(sampleProject));
        when(testCaseRepository.findByFeatureIdAndIsDeletedFalse(10)).thenReturn(List.of(sampleTestCase1));
        when(testingExecutionRepository.findByTestcaseIdIn(any())).thenReturn(List.of(sampleExecution1));
        when(bugRepository.findByFeatureIdAndIsDeletedFalse(10)).thenReturn(List.of());

        byte[] excelBytes = exportService.exportFeatureSummary(10);

        assertThat(excelBytes).isNotNull();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(3);
            assertThat(workbook.getSheet("Executive_Summary")).isNotNull();
            assertThat(workbook.getSheet("Testcase_Execution_Summary")).isNotNull();
            assertThat(workbook.getSheet("Defect_RCA_Breakdown")).isNotNull();
        }
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project does not exist")
    void testExportProjectSummary_NotFound() {
        when(projectRepository.findByProjectIdAndIsDeletedFalse(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exportService.exportProjectSummary(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found with id: 999");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when feature does not exist")
    void testExportFeatureSummary_NotFound() {
        when(featureRepository.findByFeatureIdAndIsDeletedFalse(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exportService.exportFeatureSummary(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Feature not found with id: 999");
    }
}
