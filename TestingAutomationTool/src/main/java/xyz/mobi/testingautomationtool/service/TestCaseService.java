package xyz.mobi.testingautomationtool.service;

import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelUploadResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseExecutionResponse;

import java.util.List;

public interface TestCaseService {

    TestCaseExecutionResponse createTestCaseByManual(
            TestCaseExecutionRequest request);

    ExcelUploadResponse createTestCaseByUpload(
            MultipartFile file, Integer featureId);

}