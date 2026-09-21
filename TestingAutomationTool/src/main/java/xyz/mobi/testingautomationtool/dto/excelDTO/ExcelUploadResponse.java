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
public class ExcelUploadResponse {

    private String message;

    private Integer totalRows;

    private Integer successRows;

    private Integer failedRows;

    private List<ExcelUploadErrorResponse> errors;
}