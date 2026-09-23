package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelTestCaseRow;
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

    private static final String SHEET_NAME = "TCFormat";

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


    public List<ExcelTestCaseRow> parseExcel(
            MultipartFile file) throws ExcelValidationException, ExcelProcessingException {

        validateFile(file);

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheet(SHEET_NAME);

            if (sheet == null) {
                throw new ExcelValidationException(
                        "Required sheet '" + SHEET_NAME
                                + "' was not found in the uploaded Excel file."
                );
            }

            if (sheet.getPhysicalNumberOfRows() <= 1) {
                throw new ExcelValidationException(
                        "Excel file does not contain any testcase data rows."
                );
            }

            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new ExcelValidationException(
                        "Excel header row is missing."
                );
            }

            validateHeaders(headerRow);

            List<String> errors = new ArrayList<>();

            List<ExcelTestCaseRow> rows = new ArrayList<>();

            Set<String> testcaseIds = new HashSet<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null || isBlankRow(row)) {
                    continue;
                }

                int excelRowNumber = i + 1;

                ExcelTestCaseRow excelRow = readRow(
                        headerRow,
                        row,
                        excelRowNumber
                );

                validateRow(
                        excelRow,
                        excelRowNumber,
                        errors,
                        testcaseIds
                );

                rows.add(excelRow);
            }

            if (!errors.isEmpty()) {

                throw new ExcelValidationException(
                        buildValidationMessage(errors)
                );
            }


            if (rows.isEmpty()) {

                throw new ExcelValidationException(
                        "Excel file does not contain any valid testcase rows."
                );
            }

            return rows;

        } catch (ExcelValidationException exception) {

            throw exception;

        } catch (IOException exception) {

            throw new ExcelProcessingException(
                    "Unable to read the uploaded Excel file. Details: "
                            + exception.getMessage());

        } catch (Exception exception) {

            throw new ExcelProcessingException(
                    "Unexpected error while processing the Excel file. Details: "
                            + exception.getMessage());
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

        String lowerCaseFileName =
                fileName.toLowerCase(Locale.ROOT);

        if (!lowerCaseFileName.endsWith(".xlsx")
                && !lowerCaseFileName.endsWith(".xls")) {

            throw new ExcelValidationException(
                    "Invalid file format. "
                            + "Only .xlsx and .xls files are supported."
            );
        }
    }

    private void validateHeaders(Row headerRow)
            throws ExcelValidationException {

        List<String> errors = new ArrayList<>();


        for (int i = 0; i < EXPECTED_HEADERS.size(); i++) {

            String actualHeader =
                    getCellValue(headerRow.getCell(i)).trim();

            String expectedHeader =
                    EXPECTED_HEADERS.get(i);

            if (actualHeader.isBlank()) {

                errors.add(
                        "Row 1, Column " + getColumnName(i)
                                + ": Header is blank. "
                                + "Expected '" + expectedHeader + "'."
                );

                continue;
            }

            if (!expectedHeader.equalsIgnoreCase(actualHeader)) {

                errors.add(
                        "Row 1, Column " + getColumnName(i)
                                + ": Expected header '"
                                + expectedHeader
                                + "' but found '"
                                + actualHeader
                                + "'."
                );
            }
        }

        Set<String> headers = new HashSet<>();

        int lastColumn = headerRow.getLastCellNum();

        for (int i = 0; i < lastColumn; i++) {

            String header = getCellValue(headerRow.getCell(i)).trim();

            if (header.isBlank()) {
                continue;
            }

            String normalizedHeader = header.toLowerCase(Locale.ROOT);

            if (!headers.add(normalizedHeader)) {

                errors.add(
                        "Row 1, Column " + getColumnName(i)
                                + ": Duplicate header '"
                                + header + "'."
                );
            }
        }

        if (!errors.isEmpty()) {

            throw new ExcelValidationException(
                    buildValidationMessage(errors)
            );
        }
    }

    private ExcelTestCaseRow readRow(
            Row headerRow,
            Row dataRow,
            int rowNumber) {

        return ExcelTestCaseRow.builder()
                .rowNumber(rowNumber)
                .testcaseFormatId(getCellValue(dataRow.getCell(0)))
                .title(getCellValue(dataRow.getCell(1)))
                .testType(getCellValue(dataRow.getCell(2)))
                .testExecution(getCellValue(dataRow.getCell(3)))
                .testValidation(getCellValue(dataRow.getCell(4)))
                .automationPriority(getCellValue(dataRow.getCell(5)))
                .preCondition(getCellValue(dataRow.getCell(6)))
                .testData(getCellValue(dataRow.getCell(7)))
                .executionSteps(getCellValue(dataRow.getCell(8)))
                .uiValidations(getCellValue(dataRow.getCell(9)))
                .dbValidations(getCellValue(dataRow.getCell(10)))
                .automationStatus(getCellValue(dataRow.getCell(11)))
                .actualStatus(getCellValue(dataRow.getCell(12)))
                .comments(getCellValue(dataRow.getCell(13)))
                .dynamicFields(readDynamicFields(headerRow, dataRow))
                .build();
    }

    private Map<String, Object> readDynamicFields(
            Row headerRow,
            Row dataRow) {

        Map<String, Object> dynamicFields =
                new LinkedHashMap<>();

        int lastColumn = headerRow.getLastCellNum();

        if (lastColumn <= EXPECTED_HEADERS.size()) {
            return dynamicFields;
        }

        for (int i = EXPECTED_HEADERS.size();
             i < lastColumn;
             i++) {

            String header = getCellValue(headerRow.getCell(i)).trim();

            if (header.isBlank()) {
                continue;
            }

            String value = getCellValue(dataRow.getCell(i));

            dynamicFields.put(header, value);
        }

        return dynamicFields;
    }

    private void validateRow(
            ExcelTestCaseRow row,
            int rowNumber,
            List<String> errors,
            Set<String> testcaseIds) {

        validateRequired(
                row.getTestcaseFormatId(),
                "TestcaseID",
                rowNumber,
                errors
        );


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

        if (value == null || value.trim().isBlank()) {

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


        if (value == null || value.trim().isBlank()) {
            return;
        }

        String normalizedValue = value.trim().toUpperCase(Locale.ROOT);

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

            String value =
                    getCellValue(row.getCell(i));

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

        return formatter
                .formatCellValue(cell)
                .trim();
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

        StringBuilder message =
                new StringBuilder();

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