package xyz.mobi.testingautomationtool.service.impl;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

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
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.TestCasePatchRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.TestCasePutRequest;
import xyz.mobi.testingautomationtool.dto.response.DeleteMethodDto.PatchTestCaseDeleteResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.PatchExecutionResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.PatchTestCaseResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.TestCasePutResponse;
import xyz.mobi.testingautomationtool.entity.*;
import xyz.mobi.testingautomationtool.enums.*;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;

import xyz.mobi.testingautomationtool.mapper.putMapper.TestCasePutMapper;
import xyz.mobi.testingautomationtool.repository.*;

import xyz.mobi.testingautomationtool.service.TestCaseService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final TestingExecutionRepository testingExecutionRepository;
    private final UserRepository userRepository;
    private final BugRepository bugRepository;

    private final TestCasePutMapper testCasePutMapper;

    private final Utils utils;

    @Override
    public TestCasePutResponse updateTestcaseDetails(TestCasePutRequest testCasePutRequest,
                                                     Integer id) {

        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Test case not found with ID: " + id));

        if (!testCase.isActive() || testCase.isDeleted()) {
            throw new IllegalStateException("Cannot update disabled/deleted test case with ID: " + id);
        }

        TestCase updatedTestCase = testCasePutMapper.putMethodMapper(testCasePutRequest, testCase);

        if (testCasePutRequest.getDynamicFields() != null) {
            updatedTestCase.setDynamicFields(new java.util.HashMap<>(testCasePutRequest.getDynamicFields()));
        }

        testCaseRepository.save(updatedTestCase);

        TestingExecution testingExecution = testingExecutionRepository.findByTestCase(testCase)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Execution details not found for test case ID: " + id));

        TestingExecution updatedExecution = testCasePutMapper.putMethodToExecution(testCasePutRequest,
                testingExecution);

        testingExecutionRepository.save(updatedExecution);

        return testCasePutMapper.toResponse(updatedTestCase, updatedExecution);
    }

    @Override
    @Transactional
    public String patchTestCaseDetails(
            TestCasePatchRequest request,
            Integer id) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Patch request cannot be null");
        }
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Test case not found with ID: " + id));

        if (testCase.isDeleted() || !testCase.isActive()) {
            throw new ResourceNotFoundException("Test case is deleted id:" + id);
        }

        List<String> updatedFields = new ArrayList<>();

        if (request.getTestType() != null) {
            testCase.setTestType(request.getTestType());
            updatedFields.add("testType");
        }

        if (request.getTestPriority() != null) {
            testCase.setTestPriority(request.getTestPriority());
            updatedFields.add("testPriority");
        }

        if (request.getIsActive() != null) {
            boolean active = request.getIsActive();
            testCase.setActive(active);
            if (!active) {
                bugRepository.deactivateBugs(id);
            } else {
                bugRepository.activateNonDeletedBugs(id);
            }
            updatedFields.add("isActive");
        }

        User user = userRepository.findById(1)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: "
                                        + 1));

        testCase.setUpdatedBy(user);

        if (request.getDynamicFields() != null) {
            if (testCase.getDynamicFields() == null) {
                testCase.setDynamicFields(new HashMap<>());
            }

            request.getDynamicFields().forEach((key, value) -> {
                if (value == null) {
                    testCase.getDynamicFields().remove(key);
                } else {
                    testCase.getDynamicFields().put(key, value);
                }
            });
            updatedFields.add("dynamicFields");
        }

        if (request.getComments() != null
                || request.getAutomationFeasibility() != null
                || request.getExecutionStatus() != null) {

            TestingExecution execution =
                    testingExecutionRepository
                            .findByTestCase(testCase)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Execution details not found for test case ID: "
                                                    + id));

            if (request.getComments() != null) {
                execution.setComments(request.getComments());
                updatedFields.add("comments");
            }

            if (request.getAutomationFeasibility() != null) {
                execution.setAutomationFeasibility(
                        request.getAutomationFeasibility());
                updatedFields.add("automationFeasibility");
            }

            if (request.getExecutionStatus() != null) {
                ExecutionStatus executionStatus =
                        request.getExecutionStatus();

                execution.setExecutionStatus(executionStatus);

                TestCaseStatus testCaseStatus =
                        switch (executionStatus) {
                            case PASS -> TestCaseStatus.PASSED;
                            case FAIL -> TestCaseStatus.FAILED;
                            case DESCOPE -> TestCaseStatus.DESCOPE;
                            default -> throw new IllegalArgumentException(
                                    "Unsupported execution status: "
                                            + executionStatus);
                        };

                testCase.setTestcaseStatus(testCaseStatus);

                execution.setExecutionNumber(
                        execution.getExecutionNumber() + 1);

                if (executionStatus == ExecutionStatus.PASS) {
                    utils.trigger(
                            testCase,
                            testCase.getCreatedBy(),
                            null);
                }
                updatedFields.add("executionStatus");
            }

            testingExecutionRepository.save(execution);
        }

        if (updatedFields.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one field must be provided for update");
        }

        testCaseRepository.save(testCase);

        return "Test case with ID " + id + " updated successfully. Changed fields: " + String.join(", ", updatedFields);
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
    public String hardDeleteTestCase(Integer id) {

        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Test case not found with ID: " + id));

        String testcaseFormatId = testCase.getTestcaseFormatId();

        testingExecutionRepository.findByTestCase(testCase)
                .ifPresent(testingExecutionRepository::delete);

        bugRepository.findByTestCase_TestcaseId(id)
                .ifPresent(bugRepository::deleteAll);

        testCaseRepository.delete(testCase);

        return "Test case " + testcaseFormatId + " deleted successfully";


    public PatchTestCaseDeleteResponse softDeleteTestCase(Integer id) {
        TestCase testCase = testCaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("TestCase is not present for this id:" + id));
        testCase.setDeleted(true);
        testCase.setActive(false);
        bugRepository.softDeleteBugsByTestCaseId(id);
        testCaseRepository.save(testCase);
        return PatchTestCaseDeleteResponse.builder()
                .testcaseId(id)
                .message("TestCase deleted successfully")
                .build();
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
