package xyz.mobi.testingautomationtool.dto.excelDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelUploadErrorResponse {

    private Integer row;

    private String column;

    private String value;

    private String message;
}