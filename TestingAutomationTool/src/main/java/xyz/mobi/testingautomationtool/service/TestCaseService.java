package xyz.mobi.testingautomationtool.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.dto.TestcaseDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

public interface TestCaseService {

    TestCaseExecutionResponse createTestCaseByManual(TestCaseExecutionRequest request);

    TestCaseExecutionResponse createTestCaseByUpload(TestCaseExecutionRequest request);

    Page<TestCaseResponse> getAll(
            Integer featureId,
            TestCaseStatus status,
            TestType type,
            TestPriority priority,
            Pageable pageable);

    Page<TestCaseResponse> getAll(Integer featureId, int page, int size);

    TestCaseResponse getById(Integer id, boolean includeInactive);

    TestCaseResponse getById(Integer id);
}
