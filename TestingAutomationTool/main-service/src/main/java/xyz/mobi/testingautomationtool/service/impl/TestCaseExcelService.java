package xyz.mobi.testingautomationtool.service.impl;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.ExcelDTO.ExcelTestCaseRow;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;
import xyz.mobi.testingautomationtool.exception.ExcelProcessingException;
import xyz.mobi.testingautomationtool.exception.ExcelValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class TestCaseExcelService {

    private static final List<String> EXPECTED_HEADERS = List.of(
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
    );


    public List<ExcelTestCaseRow> parseExcel(MultipartFile file)
            throws ExcelValidationException, ExcelProcessingException {

        validateFile(file);

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new ExcelValidationException(
                        "Excel file does not contain any sheets."
                );
            }

            List<ExcelTestCaseRow> rows = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            // Shared across every sheet.
            Set<String> testcaseIds = new HashSet<>();

            for (int sheetIndex = 0;
                 sheetIndex < workbook.getNumberOfSheets();
                 sheetIndex++) {

                Sheet sheet = workbook.getSheetAt(sheetIndex);
                String sheetName = sheet.getSheetName();

                // Skip completely empty sheets.
                if (sheet.getPhysicalNumberOfRows() == 0) {
                    continue;
                }

                Row headerRow = sheet.getRow(0);

                if (headerRow == null) {
                    errors.add(
                            "Sheet '" + sheetName
                                    + "': Header row is missing."
                    );
                    continue;
                }

                // Validate headers in the current sheet.
                try {
                    validateHeaders(headerRow);
                } catch (ExcelValidationException exception) {
                    errors.add(
                            "Sheet '" + sheetName + "': "
                                    + exception.getMessage()
                    );
                    continue;
                }

                // Read every testcase row in this sheet.
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                    Row dataRow = sheet.getRow(i);

                    if (dataRow == null || isBlankRow(dataRow)) {
                        continue;
                    }

                    int excelRowNumber = i + 1;

                    ExcelTestCaseRow excelRow = readRow(
                            headerRow,
                            dataRow,
                            excelRowNumber
                    );

                    List<String> rowErrors = new ArrayList<>();

                    validateRow(
                            excelRow,
                            excelRowNumber,
                            rowErrors,
                            testcaseIds
                    );

                    for (String error : rowErrors) {
                        errors.add(
                                "Sheet '" + sheetName + "': " + error
                        );
                    }

                    rows.add(excelRow);
                }
            }

            // Reject the entire workbook if any validation fails.
            if (!errors.isEmpty()) {
                throw new ExcelValidationException(
                        buildValidationMessage(errors)
                );
            }

            if (rows.isEmpty()) {
                throw new ExcelValidationException(
                        "Excel file does not contain any testcase data rows."
                );
            }

            return rows;

        } catch (ExcelValidationException exception) {
            throw exception;

        } catch (IOException exception) {
            throw new ExcelProcessingException(
                    "Unable to read the uploaded Excel file."
            );

        } catch (Exception exception) {
            throw new ExcelProcessingException(
                    "Unexpected error while processing the Excel file."
            );
        }
    }

    private void validateFile(MultipartFile file)
            throws ExcelValidationException {

        if (file == null || file.isEmpty()) {
            throw new ExcelValidationException(
                    "Uploaded Excel file is empty."
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            throw new ExcelValidationException(
                    "Uploaded Excel file name is missing."
            );
        }

        String lowerCaseFileName = fileName.toLowerCase(Locale.ROOT);

        if (!lowerCaseFileName.endsWith(".xlsx")
                && !lowerCaseFileName.endsWith(".xls")) {

            throw new ExcelValidationException(
                    "Invalid file format. "
                            + "Only .xlsx and .xls files are supported."
            );
        }
    }

    private String normalizeHeader(String header) {

        if (header == null) {
            return "";
        }

        return header.replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase(Locale.ROOT);
    }

    private void validateHeaders(Row headerRow)
            throws ExcelValidationException {

        List<String> errors = new ArrayList<>();
        Set<String> headers = new HashSet<>();

        int lastColumn = headerRow.getLastCellNum();

        if (lastColumn < 0) {
            throw new ExcelValidationException(
                    "Header row is empty."
            );
        }

        for (int i = 0; i < lastColumn; i++) {

            String header = getCellValue(
                    headerRow.getCell(i)
            ).trim();

            if (header.isBlank()) {
                continue;
            }

            String normalizedHeader = normalizeHeader(header);

            // Detect duplicates even when capitalization or
            // spacing is different.
            if (!headers.add(normalizedHeader)) {
                errors.add(
                        "Row 1, Column " + getColumnName(i)
                                + ": Duplicate header '"
                                + header + "'."
                );
            }
        }

        // Only TestcaseID is mandatory.
        // Test Case ID, TESTCASEID and testcaseid are accepted.
        if (!headers.contains(normalizeHeader("TestcaseID"))) {
            errors.add(
                    "Required column 'TestcaseID' is missing."
            );
        }

        if (!errors.isEmpty()) {
            throw new ExcelValidationException(
                    buildValidationMessage(errors)
            );
        }
    }

    private boolean isStandardHeader(String header) {

        String normalizedHeader = normalizeHeader(header);

        return EXPECTED_HEADERS.stream()
                .anyMatch(expected ->
                        normalizeHeader(expected)
                                .equals(normalizedHeader)
                );
    }


    private ExcelTestCaseRow readRow(
            Row headerRow,
            Row dataRow,
            int rowNumber) {

        Map<String, String> values = new HashMap<>();

        Map<String, Object> dynamicFields =
                new LinkedHashMap<>();

        int lastColumn = headerRow.getLastCellNum();

        for (int i = 0; i < lastColumn; i++) {

            String header = getCellValue(
                    headerRow.getCell(i)
            ).trim();

            if (header.isBlank()) {
                continue;
            }

            String value = getCellValue(
                    dataRow.getCell(i)
            );

            // Blank cells become null.
            if (value.isBlank()) {
                value = null;
            }

            String normalizedHeader = normalizeHeader(header);

            if (isStandardHeader(header)) {

                values.put(normalizedHeader, value);

            } else {

                // Keep additional columns as dynamic fields.
                dynamicFields.put(header, value);
            }
        }

        return ExcelTestCaseRow.builder()
                .rowNumber(rowNumber)

                .testcaseFormatId(
                        values.get("testcaseid")
                )

                .title(
                        values.get("title")
                )

                .testType(
                        values.get("testtype")
                )

                .testExecution(
                        values.get("testexecution")
                )

                .testValidation(
                        values.get("testvalidation")
                )

                .automationPriority(
                        values.get("automationpriority")
                )

                .preCondition(
                        values.get("precondition")
                )

                .testData(
                        values.get("testdata")
                )

                .executionSteps(
                        values.get("executionsteps")
                )

                .uiValidations(
                        values.get("uivalidations")
                )

                .dbValidations(
                        values.get("dbvalidations")
                )

                .automationStatus(
                        values.get("automationstatus")
                )

                .actualStatus(
                        values.get("actualstatus")
                )

                .comments(
                        values.get("comments")
                )

                .dynamicFields(dynamicFields)

                .build();
    }

    private void validateRow(
            ExcelTestCaseRow row,
            int rowNumber,
            List<String> errors,
            Set<String> testcaseIds) {

        // TestcaseID is mandatory.
        validateRequired(
                row.getTestcaseFormatId(),
                "TestcaseID",
                rowNumber,
                errors
        );

        // Duplicate IDs are checked across all sheets.
        if (row.getTestcaseFormatId() != null
                && !row.getTestcaseFormatId().isBlank()) {

            String testcaseId =
                    row.getTestcaseFormatId().trim();

            if (!testcaseIds.add(testcaseId)) {
                errors.add(
                        "Row " + rowNumber
                                + ", Column 'TestcaseID': "
                                + "Duplicate TestcaseID '"
                                + testcaseId
                                + "' found in the Excel file."
                );
            }
        }

        // Optional enum fields are validated only when supplied.
        validateEnum(
                row.getTestType(),
                "Test Type",
                rowNumber,
                errors,
                getEnumValues(TestType.class)
        );

        validateEnum(
                row.getAutomationPriority(),
                "Automation Priority",
                rowNumber,
                errors,
                getEnumValues(TestPriority.class)
        );

        validateEnum(
                row.getAutomationStatus(),
                "Automation Status",
                rowNumber,
                errors,
                getEnumValues(AutomationFeasibility.class)
        );

        validateEnum(
                row.getActualStatus(),
                "Actual Status",
                rowNumber,
                errors,
                getEnumValues(TestCaseStatus.class)
        );
    }

    private void validateRequired(
            String value,
            String column,
            int rowNumber,
            List<String> errors) {

        if (value == null || value.isBlank()) {
            errors.add(
                    "Row " + rowNumber
                            + ", Column '" + column
                            + "': Required value is missing."
            );
        }
    }


    private void validateEnum(
            String value,
            String column,
            int rowNumber,
            List<String> errors,
            List<String> allowedValues) {

        if (value == null || value.isBlank()) {
            return;
        }

        String normalizedValue =
                value.trim().toUpperCase(Locale.ROOT);

        if (!allowedValues.contains(normalizedValue)) {
            errors.add(
                    "Row " + rowNumber
                            + ", Column '" + column
                            + "': Invalid value '"
                            + value
                            + "'. Allowed values: "
                            + String.join(", ", allowedValues)
                            + "."
            );
        }
    }

    private <T extends Enum<T>> List<String> getEnumValues(
            Class<T> enumClass) {

        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .toList();
    }

    private boolean isBlankRow(Row row) {

        int lastColumn = row.getLastCellNum();

        if (lastColumn < 0) {
            return true;
        }

        for (int i = 0; i < lastColumn; i++) {

            String value = getCellValue(row.getCell(i));

            if (!value.isBlank()) {
                return false;
            }
        }

        return true;
    }

    private String getCellValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        DataFormatter formatter = new DataFormatter();

        return formatter.formatCellValue(cell).trim();
    }


    private String getColumnName(int columnIndex) {

        StringBuilder columnName = new StringBuilder();

        int index = columnIndex;

        while (index >= 0) {

            columnName.insert(
                    0,
                    (char) ('A' + (index % 26))
            );

            index = (index / 26) - 1;
        }

        return columnName.toString();
    }

    private String buildValidationMessage(
            List<String> errors) {

        StringBuilder message = new StringBuilder();

        message.append("Excel validation failed. ")
                .append("Total errors: ")
                .append(errors.size())
                .append(".\n");

        for (int i = 0; i < errors.size(); i++) {

            message.append("[")
                    .append(i + 1)
                    .append("] ")
                    .append(errors.get(i))
                    .append("\n");
        }

        return message.toString().trim();
    }
}