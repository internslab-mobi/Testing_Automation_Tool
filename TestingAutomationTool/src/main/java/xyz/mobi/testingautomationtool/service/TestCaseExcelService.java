package xyz.mobi.testingautomationtool.service;

import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelParseResult;

public interface TestCaseExcelService {

    ExcelParseResult parseAndValidate(
            MultipartFile file,
            Integer featureId);
}