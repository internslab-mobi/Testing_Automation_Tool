package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;
import xyz.mobi.testingautomationtool.exception.AttachmentProcessingException;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.repository.BugRepository;
import xyz.mobi.testingautomationtool.repository.FeatureRepository;
import xyz.mobi.testingautomationtool.repository.TestCaseRepository;
import xyz.mobi.testingautomationtool.repository.TestingExecutionRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelTemplateService {

    private final FeatureRepository featureRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestingExecutionRepository testingExecutionRepository;
    private final BugRepository bugRepository;

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

    public byte[] generateTemplate(Integer projectId, Integer featureId) {

        // Check feature exists AND its project is active
        Feature feature = featureRepository
                .findActiveFeatureByProject(projectId,featureId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feature not found or project is inactive: "
                                        + featureId));

        // Only non-deleted test cases
        List<TestCase> testCases =
                testCaseRepository
                        .findByFeatureFeatureIdAndIsDeletedFalse(featureId);


        // Only non-deleted bugs
        List<Bug> bugs =
                bugRepository
                        .findByFeatureFeatureIdAndIsDeletedFalse(featureId);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            Sheet testcaseSheet = workbook.createSheet("Testcase");
            Sheet bugSheet = workbook.createSheet("Bug Details");

            // Header style
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Populate sheets
            createTestcaseSheet(testcaseSheet, headerStyle, testCases);
            createBugSheet(bugSheet, headerStyle, bugs);

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new AttachmentProcessingException(
                    "Failed to generate Excel template");
        }
    }

    private void createTestcaseSheet(
            Sheet sheet,
            CellStyle headerStyle,
            List<TestCase> testCases) {

        // Headers
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < TESTCASE_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(TESTCASE_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        int rowIndex = 1;

        for (TestCase testCase : testCases) {

            Row row = sheet.createRow(rowIndex++);


            TestingExecution execution = testingExecutionRepository
                    .findByTestCaseTestcaseId(testCase.getTestcaseId())
                    .orElse(null);

            if (execution == null) {
                execution = new TestingExecution();
            }


            // 1. TestcaseID -> testcaseFormatId
            row.createCell(0).setCellValue(
                    testCase.getTestcaseFormatId() != null
                            ? testCase.getTestcaseFormatId()
                            : "");

            // 2. Title -> title
            row.createCell(1).setCellValue(
                    testCase.getTitle() != null
                            ? testCase.getTitle()
                            : "");

            // 3. Test Type
            row.createCell(2).setCellValue(
                    testCase.getTestType() != null
                            ? testCase.getTestType().name()
                            : "");

            // 4. Test Execution
            row.createCell(3).setCellValue(
                    execution.getTestExecution() != null
                            ? execution.getTestExecution()
                            : "");

            // 5. Test Validation
            row.createCell(4).setCellValue(
                    execution.getTestValidation() != null
                            ? execution.getTestValidation()
                            : "");

            // 6. Automation Priority -> priority
            row.createCell(5).setCellValue(
                    testCase.getTestPriority()!= null
                            ? testCase.getTestPriority().name()
                            : "");

            // 7. Pre Condition
            row.createCell(6).setCellValue(
                    execution.getPrecondition() != null
                            ? execution.getPrecondition()
                            : "");

            // 8. Test data
            row.createCell(7).setCellValue(
                    execution.getTestData() != null
                            ? execution.getTestData()
                            : "");

            // 9. Execution steps
            row.createCell(8).setCellValue(
                    execution.getExecutionSteps() != null
                            ? execution.getExecutionSteps()
                            : "");

            // 10. UI Validations
            row.createCell(9).setCellValue(
                    execution.getUiValidations() != null
                            ? execution.getUiValidations()
                            : "");

            // 11. DB Validations
            row.createCell(10).setCellValue(
                    execution.getDbValidations() != null
                            ? execution.getDbValidations()
                            : "");

            // 12. Automation Status -> automationFeasibility
            row.createCell(11).setCellValue(
                    execution.getAutomationFeasibility() != null
                            ? execution.getAutomationFeasibility().name()
                            : "");

            // 13. Actual Status -> testcaseStatus
            row.createCell(12).setCellValue(
                    testCase.getTestcaseStatus() != null
                            ? testCase.getTestcaseStatus().name()
                            : "");

            // 14. Comments
            row.createCell(13).setCellValue(
                    execution.getComments() != null
                            ? execution.getComments()
                            : "");
        }

        sheet.createFreezePane(0, 1);

        setTestcaseColumnWidths(sheet);
    }

    private void createBugSheet(
            Sheet sheet,
            CellStyle headerStyle,
            List<Bug> bugs) {

        // Headers
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < BUG_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(BUG_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        int rowIndex = 1;

        for (Bug bug : bugs) {

            Row row = sheet.createRow(rowIndex++);

            // 1. Feature
            row.createCell(0).setCellValue(
                    bug.getFeature() != null
                            ? bug.getFeature().getFeatureName()
                            : "");

            // 2. Bug Format ID
            row.createCell(1).setCellValue(
                    bug.getBugFormatId() != null
                            ? bug.getBugFormatId()
                            : "");

            // 3. Title
            row.createCell(2).setCellValue(
                    bug.getTitle() != null
                            ? bug.getTitle()
                            : "");

            // 4. Description
            row.createCell(3).setCellValue(
                    bug.getDescription() != null
                            ? bug.getDescription()
                            : "");

            // 5. Severity
            row.createCell(4).setCellValue(
                    bug.getSeverity() != null
                            ? bug.getSeverity().name()
                            : "");

            // 6. Priority
            row.createCell(5).setCellValue(
                    bug.getPriority() != null
                            ? bug.getPriority().name()
                            : "");

            // 7. Status
            row.createCell(6).setCellValue(
                    bug.getStatus() != null
                            ? bug.getStatus().name()
                            : "");

            // 8. Category
            row.createCell(7).setCellValue(
                    bug.getCategory() != null
                            ? bug.getCategory().name()
                            : "");

            // 9. Bug Occurrence
            row.createCell(8).setCellValue(
                    bug.getBugOccurrence() != null
                            ? bug.getBugOccurrence()
                            : 0);

            // 10. Assigned To
            row.createCell(9).setCellValue(
                    bug.getAssignedTo() != null
                            ? String.valueOf(bug.getAssignedTo().getUserId())
                            : "");
        }

        sheet.createFreezePane(0, 1);

        setBugColumnWidths(sheet);
    }


    private CellStyle createHeaderStyle(
            Workbook workbook) {

        CellStyle style =
                workbook.createCellStyle();

        Font font =
                workbook.createFont();

        font.setBold(true);
        font.setFontHeightInPoints(
                (short) 11
        );

        style.setFont(font);

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setWrapText(true);

        style.setBorderTop(
                BorderStyle.THIN
        );

        style.setBorderBottom(
                BorderStyle.THIN
        );

        style.setBorderLeft(
                BorderStyle.THIN
        );

        style.setBorderRight(
                BorderStyle.THIN
        );

        return style;
    }

    private void setTestcaseColumnWidths(
            Sheet sheet) {

        int[] widths = {
                18, 35, 18, 25, 25,
                22, 30, 30, 40, 35,
                35, 22, 22, 35
        };

        for (int i = 0;
             i < widths.length;
             i++) {

            sheet.setColumnWidth(
                    i,
                    widths[i] * 256
            );
        }
    }

    private void setBugColumnWidths(
            Sheet sheet) {

        int[] widths = {
                20, 20, 30, 40, 18,
                18, 18, 20, 18, 20
        };

        for (int i = 0;
             i < widths.length;
             i++) {

            sheet.setColumnWidth(
                    i,
                    widths[i] * 256
            );
        }
    }
}