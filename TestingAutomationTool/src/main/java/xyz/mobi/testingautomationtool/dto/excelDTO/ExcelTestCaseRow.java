package xyz.mobi.testingautomationtool.dto.excelDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelTestCaseRow {

    private Integer rowNumber;

    private String testcaseFormatId;
    private String title;
    private String testType;
    private String testExecution;
    private String testValidation;
    private String automationPriority;
    private String preCondition;
    private String testData;
    private String executionSteps;
    private String uiValidations;
    private String dbValidations;
    private String automationStatus;
    private String actualStatus;
    private String comments;
    private Map<String, Object> dynamicFields;
}