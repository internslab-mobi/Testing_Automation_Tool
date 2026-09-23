//package xyz.mobi.testingautomationtool.service.impl;
//
//import lombok.RequiredArgsConstructor;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelParseResult;
//import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelTestCaseRow;
//import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelUploadErrorResponse;
//import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
//import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
//import xyz.mobi.testingautomationtool.enums.TestPriority;
//import xyz.mobi.testingautomationtool.enums.TestType;
////import xyz.mobi.testingautomationtool.service.TestCaseExcelService;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//public class TestCaseExcelServiceImpl {
//
//    private static final String SHEET_NAME = "TCFormat";
//
//    private static final List<String> EXPECTED_HEADERS = List.of(
//            "TestcaseID",
//            "Title",
//            "Test Type",
//            "Test Execution",
//            "Test Validation",
//            "Automation Priority",
//            "Pre Condition",
//            "Test data",
//            "Execution steps",
//            "UI Validations",
//            "DB Validations",
//            "Automation Status",
//            "Actual Status",
//            "Comments"
//    );
//
//    public static ExcelParseResult parseAndValidate(
//            MultipartFile file,
//            Integer featureId) {
//
//        List<ExcelUploadErrorResponse> errors = new ArrayList<>();
//        List<ExcelTestCaseRow> rows = new ArrayList<>();
//
//        // 1. File validation
//        validateFile(file, errors);
//
//        if (!errors.isEmpty()) {
//            return ExcelParseResult.builder()
//                    .errors(errors)
//                    .rows(rows)
//                    .build();
//        }
//
//        try (InputStream inputStream = file.getInputStream();
//             Workbook workbook = new XSSFWorkbook(inputStream)) {
//
//            // 2. Check TCFormat sheet
//            Sheet sheet = workbook.getSheet(SHEET_NAME);
//
//            if (sheet == null) {
//                errors.add(ExcelUploadErrorResponse.builder()
//                        .row(0)
//                        .column("Sheet")
//                        .value(SHEET_NAME)
//                        .message("Required sheet 'TCFormat' not found")
//                        .build());
//
//                return ExcelParseResult.builder()
//                        .errors(errors)
//                        .rows(rows)
//                        .build();
//            }
//
//            // 3. Validate headers
//            validateHeaders(sheet, errors);
//
//            if (!errors.isEmpty()) {
//                return ExcelParseResult.builder()
//                        .errors(errors)
//                        .rows(rows)
//                        .build();
//            }
//
//            // 4. Read rows
//            readRows(sheet, rows, errors);
//
//        } catch (IOException | RuntimeException e) {
//
//            errors.add(ExcelUploadErrorResponse.builder()
//                    .row(0)
//                    .column("File")
//                    .value(file.getOriginalFilename())
//                    .message("Unable to read Excel file: " + e.getMessage())
//                    .build());
//        }
//
//        // 5. Check duplicate TestcaseID inside Excel
//        validateDuplicateTestcaseIds(rows, errors);
//
//        return ExcelParseResult.builder()
//                .errors(errors)
//                .rows(rows)
//                .build();
//    }
//
//    private static void validateFile(
//            MultipartFile file,
//            List<ExcelUploadErrorResponse> errors) {
//
//        if (file == null || file.isEmpty()) {
//            errors.add(ExcelUploadErrorResponse.builder()
//                    .row(0)
//                    .column("File")
//                    .value("")
//                    .message("Excel file is required")
//                    .build());
//
//            return;
//        }
//
//        String filename = file.getOriginalFilename();
//
//        if (filename == null ||
//                !filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
//
//            errors.add(ExcelUploadErrorResponse.builder()
//                    .row(0)
//                    .column("File")
//                    .value(filename == null ? "" : filename)
//                    .message("Only .xlsx Excel files are supported")
//                    .build());
//        }
//    }
//
//    private static void validateHeaders(
//            Sheet sheet,
//            List<ExcelUploadErrorResponse> errors) {
//
//        Row headerRow = sheet.getRow(0);
//
//        if (headerRow == null) {
//            errors.add(ExcelUploadErrorResponse.builder()
//                    .row(1)
//                    .column("Header")
//                    .value("")
//                    .message("Header row is missing")
//                    .build());
//
//            return;
//        }
//
//        for (int i = 0; i < EXPECTED_HEADERS.size(); i++) {
//
//            String actualHeader = getCellValue(
//                    headerRow.getCell(i));
//
//            String expectedHeader = EXPECTED_HEADERS.get(i);
//
//            if (!expectedHeader.equals(actualHeader)) {
//
//                errors.add(ExcelUploadErrorResponse.builder()
//                        .row(1)
//                        .column("Column " + (i + 1))
//                        .value(actualHeader)
//                        .message("Expected header '" +
//                                expectedHeader +
//                                "'")
//                        .build());
//            }
//        }
//    }
//
//    private static void readRows(
//            Sheet sheet,
//            List<ExcelTestCaseRow> rows,
//            List<ExcelUploadErrorResponse> errors) {
//
//        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//
//            Row row = sheet.getRow(i);
//
//            // Completely blank rows are ignored
//            if (isBlankRow(row)) {
//                continue;
//            }
//
//            int excelRowNumber = i + 1;
//
//            ExcelTestCaseRow testCaseRow =
//                    ExcelTestCaseRow.builder()
//                            .rowNumber(excelRowNumber)
//                            .testcaseFormatId(getCellValue(row.getCell(0)))
//                            .title(getCellValue(row.getCell(1)))
//                            .testType(getCellValue(row.getCell(2)))
//                            .testExecution(getCellValue(row.getCell(3)))
//                            .testValidation(getCellValue(row.getCell(4)))
//                            .automationPriority(getCellValue(row.getCell(5)))
//                            .preCondition(getCellValue(row.getCell(6)))
//                            .testData(getCellValue(row.getCell(7)))
//                            .executionSteps(getCellValue(row.getCell(8)))
//                            .uiValidations(getCellValue(row.getCell(9)))
//                            .dbValidations(getCellValue(row.getCell(10)))
//                            .automationStatus(getCellValue(row.getCell(11)))
//                            .actualStatus(getCellValue(row.getCell(12)))
//                            .comments(getCellValue(row.getCell(13)))
//                            .build();
//
//            rows.add(testCaseRow);
//
//            validateRequiredFields(testCaseRow, errors);
//
//            validateEnumValues(testCaseRow, errors);
//        }
//    }
//
//    private static void validateRequiredFields(
//            ExcelTestCaseRow row,
//            List<ExcelUploadErrorResponse> errors) {
//
//        if (isBlank(row.getTestcaseFormatId())) {
//            addError(
//                    row,
//                    "TestcaseID",
//                    row.getTestcaseFormatId(),
//                    "TestcaseID is required",
//                    errors);
//        }
//
//        if (isBlank(row.getTitle())) {
//            addError(
//                    row,
//                    "Title",
//                    row.getTitle(),
//                    "Title is required",
//                    errors);
//        }
//
//        if (isBlank(row.getTestType())) {
//            addError(
//                    row,
//                    "Test Type",
//                    row.getTestType(),
//                    "Test Type is required",
//                    errors);
//        }
//
//        if (isBlank(row.getAutomationPriority())) {
//            addError(
//                    row,
//                    "Automation Priority",
//                    row.getAutomationPriority(),
//                    "Automation Priority is required",
//                    errors);
//        }
//    }
//
//    private static void validateEnumValues(
//            ExcelTestCaseRow row,
//            List<ExcelUploadErrorResponse> errors) {
//
//        if (!isBlank(row.getTestType())) {
//            validateEnum(
//                    row,
//                    "Test Type",
//                    row.getTestType(),
//                    TestType.class,
//                    errors);
//        }
//
//        if (!isBlank(row.getAutomationPriority())) {
//            validateEnum(
//                    row,
//                    "Automation Priority",
//                    row.getAutomationPriority(),
//                    TestPriority.class,
//                    errors);
//        }
//
//        if (!isBlank(row.getAutomationStatus())) {
//            validateEnum(
//                    row,
//                    "Automation Status",
//                    row.getAutomationStatus(),
//                    AutomationFeasibility.class,
//                    errors);
//        }
//
//        if (!isBlank(row.getActualStatus())) {
//            validateEnum(
//                    row,
//                    "Actual Status",
//                    row.getActualStatus(),
//                    TestCaseStatus.class,
//                    errors);
//        }
//    }
//
//    private static <E extends Enum<E>> void validateEnum(
//            ExcelTestCaseRow row,
//            String column,
//            String value,
//            Class<E> enumClass,
//            List<ExcelUploadErrorResponse> errors) {
//
//        try {
//
//            Enum.valueOf(
//                    enumClass,
//                    value.trim().toUpperCase()
//            );
//
//        } catch (IllegalArgumentException e) {
//
//            addError(
//                    row,
//                    column,
//                    value,
//                    "Invalid value. Allowed values are: "
//                            + Arrays.toString(
//                            enumClass.getEnumConstants()
//                    ),
//                    errors
//            );
//        }
//    }
//
//    private static void validateDuplicateTestcaseIds(
//            List<ExcelTestCaseRow> rows,
//            List<ExcelUploadErrorResponse> errors) {
//
//        Map<String, ExcelTestCaseRow> seen = new HashMap<>();
//
//        for (ExcelTestCaseRow row : rows) {
//
//            String testcaseId = normalize(
//                    row.getTestcaseFormatId());
//
//            if (testcaseId.isEmpty()) {
//                continue;
//            }
//
//            if (seen.containsKey(testcaseId)) {
//
//                addError(
//                        row,
//                        "TestcaseID",
//                        row.getTestcaseFormatId(),
//                        "Duplicate TestcaseID in Excel. " +
//                                "First occurrence is at row " +
//                                seen.get(testcaseId).getRowNumber(),
//                        errors);
//
//            } else {
//                seen.put(testcaseId, row);
//            }
//        }
//    }
//
//    private static boolean isBlankRow(Row row) {
//
//        if (row == null) {
//            return true;
//        }
//
//        for (int i = 0; i < EXPECTED_HEADERS.size(); i++) {
//
//            if (!getCellValue(row.getCell(i)).trim().isEmpty()) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    private static String getCellValue(Cell cell) {
//
//        if (cell == null) {
//            return "";
//        }
//
//        DataFormatter formatter = new DataFormatter();
//
//        return formatter.formatCellValue(cell).trim();
//    }
//
//    private static boolean isBlank(String value) {
//
//        return value == null ||
//                value.trim().isEmpty();
//    }
//
//    private static String normalize(String value) {
//
//        return value == null
//                ? ""
//                : value.trim().toLowerCase(Locale.ROOT);
//    }
//
//    private static void addError(
//            ExcelTestCaseRow row,
//            String column,
//            String value,
//            String message,
//            List<ExcelUploadErrorResponse> errors) {
//
//        errors.add(ExcelUploadErrorResponse.builder()
//                .row(row.getRowNumber())
//                .column(column)
//                .value(value)
//                .message(message)
//                .build());
//    }
//}