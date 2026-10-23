package xyz.mobi.summary_export_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.summary_export_service.entity.Bug;
import xyz.mobi.summary_export_service.entity.Feature;
import xyz.mobi.summary_export_service.entity.TestCase;
import xyz.mobi.summary_export_service.entity.TestingExecution;
import xyz.mobi.summary_export_service.exception.ResourceNotFoundException;
import xyz.mobi.summary_export_service.repository.BugRepository;
import xyz.mobi.summary_export_service.repository.FeatureRepository;
import xyz.mobi.summary_export_service.repository.TestCaseRepository;
import xyz.mobi.summary_export_service.repository.TestingExecutionRepository;
import xyz.mobi.summary_export_service.service.ExcelTemplateService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelTemplateServiceImpl implements ExcelTemplateService {

    private final FeatureRepository featureRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestingExecutionRepository testingExecutionRepository;
    private final BugRepository bugRepository;
    private static final int MAX_TEST_CASES = 1000;
    private static final String[] TESTCASE_HEADERS = {
            "TestcaseID",
            "Title",
            "Test Type",
            "Test Execution",
            "Test Validation",
            "Automation Priority",
            "Pre Condition",
            "Test data",
            "Execution steps",
            "UI Validations",
            "DB Validations",
            "Automation Status",
            "Actual Status",
            "Comments"
    };

    private static final String[] BUG_HEADERS = {
            "Feature",
            "Bug Format ID",
            "Title",
            "Description",
            "Severity",
            "Priority",
            "Status",
            "Category",
            "Bug Occurrence",
            "Assigned To"
    };

    @Override
    @Transactional(readOnly = true)
    public byte[] generateTemplate(Integer projectId, Integer featureId) {
        log.info("Generating Excel template for Project ID: {}, Feature ID: {}", projectId, featureId);

        // Check feature exists AND belongs to project (and is active/non-deleted)
        Feature feature = featureRepository.findActiveFeatureByProject(projectId, featureId)
                .or(() -> featureRepository.findByProjectIdAndFeatureIdAndIsDeletedFalse(projectId, featureId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Feature not found or project is inactive: " + featureId));

        // Only non-deleted test cases
        List<TestCase> testCases = testCaseRepository.findByFeatureIdAndIsDeletedFalse(featureId);

        // Batch-load executions for performance
        List<Integer> testCaseIds = testCases.stream().map(TestCase::getTestcaseId).toList();
        List<TestingExecution> executions = testCaseIds.isEmpty() ? Collections.emptyList()
                : testingExecutionRepository.findByTestcaseIdIn(testCaseIds);

        Map<Integer, TestingExecution> executionMap = executions.stream()
                .collect(Collectors.toMap(
                        TestingExecution::getTestcaseId,
                        e -> e,
                        (existing, replacement) -> (replacement.getExecutionId() != null && existing.getExecutionId() != null
                                && replacement.getExecutionId() > existing.getExecutionId()) ? replacement : existing
                ));

        // Only non-deleted bugs
        List<Bug> bugs = bugRepository.findByFeatureIdAndIsDeletedFalse(featureId);

        // Use SXSSFWorkbook(100) streaming to prevent OOM
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            workbook.setCompressTempFiles(true);

            SXSSFSheet testcaseSheet = workbook.createSheet("Testcase");
            SXSSFSheet bugSheet = workbook.createSheet("Bug Details");

            testcaseSheet.setDisplayGridlines(true);
            bugSheet.setDisplayGridlines(true);

            // Header style
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Populate sheets
            createTestcaseSheet(testcaseSheet, headerStyle, testCases, executionMap);
            createBugSheet(bugSheet, headerStyle, bugs, feature.getFeatureName());

            workbook.write(outputStream);
            workbook.dispose(); // clean up temp files

            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate Excel template: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Excel template: " + e.getMessage(), e);
        }
    }

    private void createTestcaseSheet(
            SXSSFSheet sheet,
            CellStyle headerStyle,
            List<TestCase> testCases,
            Map<Integer, TestingExecution> executionMap) {

        // Headers
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(24);

        for (int i = 0; i < TESTCASE_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(TESTCASE_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        int rowIndex = 1;

        for (TestCase testCase : testCases) {
            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(20);

            TestingExecution execution = executionMap.getOrDefault(testCase.getTestcaseId(), new TestingExecution());

            // 1. TestcaseID -> testcaseFormatId
            row.createCell(0).setCellValue(
                    testCase.getTestcaseFormatId() != null ? testCase.getTestcaseFormatId() : "");

            // 2. Title -> title
            row.createCell(1).setCellValue(
                    testCase.getTitle() != null ? testCase.getTitle() : "");

            // 3. Test Type
            row.createCell(2).setCellValue(
                    testCase.getTestType() != null ? testCase.getTestType() : "");

            // 4. Test Execution
            row.createCell(3).setCellValue(
                    execution.getTestExecution() != null ? execution.getTestExecution() : "");

            // 5. Test Validation
            row.createCell(4).setCellValue(
                    execution.getTestValidation() != null ? execution.getTestValidation() : "");

            // 6. Automation Priority -> priority
            row.createCell(5).setCellValue(
                    testCase.getTestPriority() != null ? testCase.getTestPriority() : "");

            // 7. Pre Condition
            row.createCell(6).setCellValue(
                    execution.getPrecondition() != null ? execution.getPrecondition() : "");

            // 8. Test data
            row.createCell(7).setCellValue(
                    execution.getTestData() != null ? execution.getTestData() : "");

            // 9. Execution steps
            row.createCell(8).setCellValue(
                    execution.getExecutionSteps() != null ? execution.getExecutionSteps() : "");

            // 10. UI Validations
            row.createCell(9).setCellValue(
                    execution.getUiValidations() != null ? execution.getUiValidations() : "");

            // 11. DB Validations
            row.createCell(10).setCellValue(
                    execution.getDbValidations() != null ? execution.getDbValidations() : "");

            // 12. Automation Status -> automationFeasibility
            row.createCell(11).setCellValue(
                    execution.getAutomationFeasibility() != null ? execution.getAutomationFeasibility() : "");

            // 13. Actual Status -> testcaseStatus
            row.createCell(12).setCellValue(
                    testCase.getTestcaseStatus() != null ? testCase.getTestcaseStatus() : "");

            // 14. Comments
            row.createCell(13).setCellValue(
                    execution.getComments() != null ? execution.getComments() : "");
        }

        sheet.createFreezePane(0, 1);
        setTestcaseColumnWidths(sheet);
    }

    private void createBugSheet(
            SXSSFSheet sheet,
            CellStyle headerStyle,
            List<Bug> bugs,
            String featureName) {

        // Headers
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(24);

        for (int i = 0; i < BUG_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(BUG_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        int rowIndex = 1;

        for (Bug bug : bugs) {
            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(20);

            // 1. Feature
            String fName = (bug.getFeature() != null && bug.getFeature().getFeatureName() != null)
                    ? bug.getFeature().getFeatureName()
                    : featureName;
            row.createCell(0).setCellValue(fName != null ? fName : "");

            // 2. Bug Format ID
            row.createCell(1).setCellValue(
                    bug.getBugFormatId() != null ? bug.getBugFormatId() : "");

            // 3. Title
            row.createCell(2).setCellValue(
                    bug.getTitle() != null ? bug.getTitle() : "");

            // 4. Description
            row.createCell(3).setCellValue(
                    bug.getDescription() != null ? bug.getDescription() : "");

            // 5. Severity
            row.createCell(4).setCellValue(
                    bug.getSeverity() != null ? bug.getSeverity().name() : "");

            // 6. Priority
            row.createCell(5).setCellValue(
                    bug.getPriority() != null ? bug.getPriority().name() : "");

            // 7. Status
            row.createCell(6).setCellValue(
                    bug.getStatus() != null ? bug.getStatus().name() : "");

            // 8. Category
            row.createCell(7).setCellValue(
                    bug.getCategory() != null ? bug.getCategory().name() : "");

            // 9. Bug Occurrence
            row.createCell(8).setCellValue(
                    bug.getBugOccurrence() != null ? bug.getBugOccurrence() : 0);

            // 10. Assigned To
            row.createCell(9).setCellValue(
                    bug.getAssignedTo() != null && bug.getAssignedTo().getUserId() != null
                            ? String.valueOf(bug.getAssignedTo().getUserId())
                            : "");
        }

        sheet.createFreezePane(0, 1);
        setBugColumnWidths(sheet);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);

        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private void setTestcaseColumnWidths(SXSSFSheet sheet) {
        int[] widths = {
                18, 35, 18, 25, 25,
                22, 30, 30, 40, 35,
                35, 22, 22, 35
        };

        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
    }

    private void setBugColumnWidths(SXSSFSheet sheet) {
        int[] widths = {
                20, 20, 30, 40, 18,
                18, 18, 20, 18, 20
        };

        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
    }

    @Override
    public void generateTemplate(OutputStream outputStream)
            throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Test Cases");

            // Header styling
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(
                    IndexedColors.DARK_BLUE.getIndex()
            );
            headerStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );
            headerStyle.setWrapText(true);
            headerStyle.setVerticalAlignment(
                    VerticalAlignment.CENTER
            );

            // Optional style to indicate the Title column is required
            CellStyle requiredStyle = workbook.createCellStyle();
            requiredStyle.setFillForegroundColor(
                    IndexedColors.LIGHT_YELLOW.getIndex()
            );
            requiredStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );

            // Create headers
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(30);

            for (int i = 0; i < TESTCASE_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(TESTCASE_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 22 * 256);
            }

            // Create 1,000 data rows
            for (int rowIndex = 1;
                 rowIndex <= MAX_TEST_CASES;
                 rowIndex++) {

                Row row = sheet.createRow(rowIndex);

                // Title is column index 1
                row.createCell(1).setCellStyle(requiredStyle);
            }

            // Dropdowns
            addDropdown(sheet, 2,
                    new String[]{"API", "DB", "E2E", "UI"});

            addDropdown(sheet, 5,
                    new String[]{"HIGH", "LOW", "MEDIUM"});

            addDropdown(sheet, 11,
                    new String[]{
                            "YES", "NO", "PARTIALLY_FEASIBLE"
                    });

            addDropdown(sheet, 12,
                    new String[]{
                            "DESCOPE", "FAIL", "PASS",
                            "IN_PROGRESS", "BLOCK"
                    });

            // Freeze header and enable filtering
            sheet.createFreezePane(0, 1);

            sheet.setAutoFilter(
                    new CellRangeAddress(
                            0,
                            MAX_TEST_CASES,
                            0,
                            TESTCASE_HEADERS.length - 1
                    )
            );

            workbook.write(outputStream);
        }
    }

    private void addDropdown(
            Sheet sheet,
            int columnIndex,
            String[] values) {

        DataValidationHelper helper =
                sheet.getDataValidationHelper();

        DataValidationConstraint constraint =
                helper.createExplicitListConstraint(values);

        CellRangeAddressList addressList =
                new CellRangeAddressList(
                        1,
                        MAX_TEST_CASES,
                        columnIndex,
                        columnIndex
                );

        DataValidation validation =
                helper.createValidation(
                        constraint,
                        addressList
                );

        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);

        validation.createErrorBox(
                "Invalid value",
                "Please select an allowed value from the dropdown."
        );

        sheet.addValidationData(validation);
    }
}
