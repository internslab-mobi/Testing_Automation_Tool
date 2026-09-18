package xyz.mobi.testingautomationtool.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.dto.TestcaseDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.dto.TestingExecutionDTO.TestingExecutionResponse;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;
import xyz.mobi.testingautomationtool.enums.ExecutionStatus;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.mapper.TestCaseMapper;
import xyz.mobi.testingautomationtool.repository.TestCaseRepository;
import xyz.mobi.testingautomationtool.repository.TestingExecutionRepository;
import xyz.mobi.testingautomationtool.service.TestCaseService;

@Service
@Transactional
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final TestingExecutionRepository testingExecutionRepository;
    private final TestCaseMapper testCaseMapper;

    public TestCaseServiceImpl(TestCaseRepository testCaseRepository,
                               TestingExecutionRepository testingExecutionRepository,
                               TestCaseMapper testCaseMapper) {
        this.testCaseRepository = testCaseRepository;
        this.testingExecutionRepository = testingExecutionRepository;
        this.testCaseMapper = testCaseMapper;
    }

    @Override
    public TestCaseExecutionResponse createTestCaseByManual(
            TestCaseExecutionRequest request) {

        String formatId = request.getTestCase().getTestcaseFormatId();

        if (testCaseRepository.existsByTestcaseFormatId(formatId)) {
                throw new IllegalArgumentException(
                    "Test case format ID already exists: " + formatId
            );
        }

            TestCase testcase = new TestCase();

            testcase.setFeatureId(request.getTestCase().getFeatureId());
            testcase.setTitle(request.getTestCase().getTitle());
            testcase.setTestType(request.getTestCase().getTestType());
            testcase.setTestPriority(request.getTestCase().getTestPriority());
            testcase.setTestcaseStatus(TestCaseStatus.NO_RUN);
            testcase.setTestcaseFormatId(request.getTestCase().getTestcaseFormatId());
            testcase.setCreatedBy(request.getTestCase().getCreatedBy());

            TestCase savedTestCase = testCaseRepository.save(testcase);

            TestingExecution testingExecution = new TestingExecution();

            testingExecution.setTestCase(savedTestCase);
            testingExecution.setBugsCount(0);
            testingExecution.setExecutionNumber(0);
            testingExecution.setAutomationFeasibility(request.getTestingExecutionRequest().getAutomationFeasibility());
            testingExecution.setExecutionStatus(ExecutionStatus.NO_RUN);
            testingExecution.setTestExecution(request.getTestingExecutionRequest().getTestExecution());
            testingExecution.setTestValidation(request.getTestingExecutionRequest().getTestValidation());
            testingExecution.setPrecondition(request.getTestingExecutionRequest().getPrecondition());
            testingExecution.setTestData(request.getTestingExecutionRequest().getTestData());
            testingExecution.setExecutionSteps(request.getTestingExecutionRequest().getExecutionSteps());
            testingExecution.setUiValidations(request.getTestingExecutionRequest().getUiValidations());
            testingExecution.setDbValidations(request.getTestingExecutionRequest().getDbValidations());
            testingExecution.setComments(request.getTestingExecutionRequest().getComments());
            testingExecution.setExecutedBy(0);

            TestingExecution savedTestExecution = testingExecutionRepository.save(testingExecution);

            TestCaseResponse testCaseResponse =
                    testCaseMapper.toResponse(savedTestCase);

            TestingExecutionResponse testingExecutionResponse =
                    testCaseMapper.toResponse(savedTestExecution);

            return TestCaseExecutionResponse.builder()
                    .testCaseResponse(testCaseResponse)
                    .testingExecutionResponse(testingExecutionResponse)
                    .build();
    }


    @Override
    public TestCaseExecutionResponse createTestCaseByUpload(TestCaseExecutionRequest request) {
        return null;
    }

}