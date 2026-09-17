package xyz.mobi.testingautomationtool.service.impl;

import org.springframework.stereotype.Service;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.repository.TestCaseRepository;
import xyz.mobi.testingautomationtool.repository.TestingExecutionRepository;
import xyz.mobi.testingautomationtool.service.TestCaseService;

@Service
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final TestingExecutionRepository testingExecutionRepository;

    public TestCaseServiceImpl(TestCaseRepository testCaseRepository,
                               TestingExecutionRepository testingExecutionRepository) {
        this.testCaseRepository = testCaseRepository;
        this.testingExecutionRepository = testingExecutionRepository;
    }


    @Override
    public TestCaseExecutionResponse createTestCaseByManual(
            TestCaseExecutionRequest request) {
            return null;
    }

    @Override
    public TestCaseExecutionResponse createTestCaseByUpload(TestCaseExecutionRequest request) {
        return null;
    }

    @Override
    public TestCaseExecutionResponse getById(int id) { return null; }

    @Override
    public TestCaseExecutionResponse getByAll() {
        return null;
    }











