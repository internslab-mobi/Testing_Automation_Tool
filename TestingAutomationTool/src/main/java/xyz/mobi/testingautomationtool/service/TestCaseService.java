package xyz.mobi.testingautomationtool.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.ExcelDTO.ExcelUploadResponse;
import xyz.mobi.testingautomationtool.dto.TestCaseDTO.*;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

public interface TestCaseService {

    TestCaseExecutionResponse createTestCaseByManual(TestCaseExecutionRequest request);

    ExcelUploadResponse createTestCaseByUpload(MultipartFile file, Integer featureId);

    byte[] downloadTemplate(Integer projectId, Integer featureId);

    Page<TestCaseResponse> getAll(
            Integer featureId,
            TestCaseStatus status,
            TestType type,
            TestPriority priority,
            Pageable pageable);

    Page<TestCaseResponse> getAll(Integer featureId, int page, int size);

    TestCaseResponse getById(Integer id, boolean includeInactive);

    TestCaseResponse getById(Integer id);

    Page<TestCaseResponse> searchTestCases(
            String keyword,
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
}
