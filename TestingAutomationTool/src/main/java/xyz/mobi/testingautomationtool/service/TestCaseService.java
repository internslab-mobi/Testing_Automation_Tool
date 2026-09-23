package xyz.mobi.testingautomationtool.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.dto.TestcaseDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.TestCasePatchRequest;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.UpdateExecutionStatusRequest;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelUploadResponse;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.TestCasePutRequest;
import xyz.mobi.testingautomationtool.dto.response.DeleteMethodDto.PatchTestCaseDeleteResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.ExecutionStatusResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.PatchTestCaseResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.TestCasePutResponse;

import java.util.List;

public interface TestCaseService {

    TestCaseExecutionResponse createTestCaseByManual(TestCaseExecutionRequest request);

    TestCaseExecutionResponse createTestCaseByUpload(TestCaseExecutionRequest request);

    Page<TestCaseResponse> getAll(
            Integer featureId,
            TestCaseStatus status,
            TestType type,
            TestPriority priority,
            Pageable pageable);

    TestCasePutResponse updateTestcaseDetails(
            TestCasePutRequest testCaseRequest,
            Integer id);

    String patchTestCaseDetails(
            TestCasePatchRequest testPatchMethodDto,
            Integer id);

    String hardDeleteTestCase(Integer id);

    PatchTestCaseDeleteResponse softDeleteTestCase(Integer id);
    Page<TestCaseResponse> getAll(Integer featureId, int page, int size);

    TestCaseResponse getById(Integer id, boolean includeInactive);

    TestCaseResponse getById(Integer id);
}
