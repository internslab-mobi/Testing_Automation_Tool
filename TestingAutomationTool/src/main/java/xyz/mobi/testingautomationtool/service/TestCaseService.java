package xyz.mobi.testingautomationtool.service;

import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.TestCasePatchRequest;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.UpdateExecutionStatusRequest;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelUploadResponse;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.TestCasePutRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.TestCasePutResponse;

import java.util.List;

public interface TestCaseService {

    TestCaseExecutionResponse createTestCaseByManual(
            TestCaseExecutionRequest request);

    ExcelUploadResponse createTestCaseByUpload(
            MultipartFile file, Integer featureId);

    TestCaseExecutionResponse getById(int id);

    List<TestCaseExecutionResponse> getByAll();

    List<TestCaseExecutionResponse> getByFeatureId(Integer featureId);

    TestCasePutResponse updateTestcaseDetails(
            TestCasePutRequest testCaseRequest,
            Integer id);

    TestCaseResponse patchTestCaseDetails(
            TestCasePatchRequest testPatchMethodDto,
            Integer id);

    String softDeleteTestCase(Integer id);

    String hardDeleteTestCase(Integer id);

    TestCaseExecutionResponse updateExecutionStatus(
            Integer executionId,
            UpdateExecutionStatusRequest request);

}
