package xyz.mobi.testingautomationtool.dto.excelDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelParseResult {

    private List<ExcelUploadErrorResponse> errors;

    private List<ExcelTestCaseRow> rows;
}