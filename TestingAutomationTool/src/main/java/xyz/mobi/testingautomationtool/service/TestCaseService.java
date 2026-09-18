package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionResponse;

public interface TestCaseService {

    TestCaseExecutionResponse createTestCaseByManual(
            TestCaseExecutionRequest request);

    TestCaseExecutionResponse createTestCaseByUpload(
            TestCaseExecutionRequest request);

}
