package xyz.mobi.testingautomationtool.service.impl;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.dto.TestcaseDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.mapper.TestCaseMapper;
import xyz.mobi.testingautomationtool.repository.TestCaseRepository;
import xyz.mobi.testingautomationtool.repository.TestingExecutionRepository;
import xyz.mobi.testingautomationtool.service.TestCaseService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final TestingExecutionRepository testingExecutionRepository;
    private final TestCaseMapper testCaseMapper;

    public TestCaseServiceImpl(
            TestCaseRepository testCaseRepository,
            TestingExecutionRepository testingExecutionRepository,
            TestCaseMapper testCaseMapper) {
        this.testCaseRepository = testCaseRepository;
        this.testingExecutionRepository = testingExecutionRepository;
        this.testCaseMapper = testCaseMapper;
    }

    @Override
    public TestCaseExecutionResponse createTestCaseByManual(
            TestCaseExecutionRequest request) {
        return null;
    }

    @Override
    public TestCaseExecutionResponse createTestCaseByUpload(
            TestCaseExecutionRequest request) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TestCaseResponse> getAll(
            Integer featureId,
            TestCaseStatus status,
            TestType type,
            TestPriority priority,
            Pageable pageable) {

        // Step 1: Validate page and size inputs
        if (pageable.getPageNumber() < 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        if (pageable.getPageSize() <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // Step 2: Cap max page size to 100 to prevent uncontrolled DB fetch
        int boundedSize = Math.min(pageable.getPageSize(), 100);
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), boundedSize, pageable.getSort());

        // Step 3: Fetch filtered test cases for the feature
        Page<TestCase> testCasesPage = testCaseRepository.findByFeatureIdWithFilters(
                featureId, status, type, priority, safePageable);

        // Step 4: Map test cases with their executions (batch fetch)
        return mapTestCasesWithExecutions(testCasesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TestCaseResponse> getAll(Integer featureId, int page, int size) {
        return getAll(featureId, null, null, null, PageRequest.of(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public TestCaseResponse getById(Integer id, boolean includeInactive) {
        // Step 1: Distinct check for test case existence
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (testCase.isDeleted()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // Step 2: Distinct check for active status vs inactive
        if (!includeInactive && !testCase.isActive()) {
            throw new CustomException(ErrorCode.BUSINESS_RULE_VIOLATION);
        }

        // Step 3: Treat execution as optional (do not throw if test case hasn't run yet)
        TestingExecution execution = testingExecutionRepository
                .findByTestCaseTestcaseId(id)
                .orElse(null);

        return testCaseMapper.toResponse(testCase, execution);
    }

    @Override
    @Transactional(readOnly = true)
    public TestCaseResponse getById(Integer id) {
        return getById(id, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TestCaseResponse> searchTestCases(
            String keyword,
            Integer featureId,
            TestCaseStatus status,
            TestType type,
            TestPriority priority,
            Pageable pageable) {

        int boundedSize = Math.min(pageable.getPageSize() <= 0 ? 10 : pageable.getPageSize(), 100);
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), boundedSize, pageable.getSort());

        Specification<TestCase> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));

            if (featureId != null) {
                predicates.add(cb.equal(root.get("feature").get("featureId"), featureId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("testcaseStatus"), status));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("testType"), type));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("testPriority"), priority));
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate formatMatch = cb.like(cb.lower(root.get("testcaseFormatId")), pattern);
                predicates.add(cb.or(titleMatch, formatMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TestCase> testCasesPage = testCaseRepository.findAll(spec, safePageable);
        return mapTestCasesWithExecutions(testCasesPage);
    }

    private Page<TestCaseResponse> mapTestCasesWithExecutions(Page<TestCase> testCasesPage) {
        List<Integer> testCaseIds = testCasesPage.getContent().stream()
                .map(TestCase::getTestcaseId)
                .toList();

        Map<Integer, TestingExecution> executionMap = Collections.emptyMap();
        if (!testCaseIds.isEmpty()) {
            List<TestingExecution> executions = testingExecutionRepository.findByTestCaseTestcaseIdIn(testCaseIds);
            executionMap = executions.stream()
                    .collect(Collectors.toMap(
                            e -> e.getTestCase().getTestcaseId(),
                            e -> e,
                            (existing, replacement) -> existing
                    ));
        }

        final Map<Integer, TestingExecution> finalExecutionMap = executionMap;
        return testCasesPage.map(testCase ->
                testCaseMapper.toResponse(testCase, finalExecutionMap.get(testCase.getTestcaseId()))
        );
    }
}
