package xyz.mobi.testingautomationtool.service.impl;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.ExcelDto.ExcelTestCaseRow;
import xyz.mobi.testingautomationtool.dto.ExcelDto.ExcelUploadErrorResponse;
import xyz.mobi.testingautomationtool.dto.ExcelDto.ExcelUploadResponse;
import xyz.mobi.testingautomationtool.dto.TestCaseDto.*;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDto.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDto.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDto.UpdateExecutionStatusRequest;
import xyz.mobi.testingautomationtool.dto.TestingExecutionDto.TestingExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestingExecutionDto.TestingExecutionResponse;
import xyz.mobi.testingautomationtool.entity.*;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
import xyz.mobi.testingautomationtool.enums.ExecutionStatus;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.TestCaseMapper;
import xyz.mobi.testingautomationtool.mapper.TestingExecutionMapper;
import xyz.mobi.testingautomationtool.repository.BugRepository;
import xyz.mobi.testingautomationtool.repository.FeatureRepository;
import xyz.mobi.testingautomationtool.repository.TestCaseRepository;
import xyz.mobi.testingautomationtool.repository.TestingExecutionRepository;
import xyz.mobi.testingautomationtool.service.AuthService;
import xyz.mobi.testingautomationtool.service.InAppNotificationService;
import xyz.mobi.testingautomationtool.service.TestCaseService;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final TestingExecutionRepository testingExecutionRepository;
    private final FeatureRepository featureRepository;
    private final BugRepository bugRepository;
    private final TestCaseMapper testCaseMapper;
    private final TestingExecutionMapper testingExecutionMapper;
    private final AuthService authService;
    private final InAppNotificationService inAppNotificationService;
    private final TestCaseExcelService testCaseExcelService;
    private final ExcelTemplateService excelTemplateService;

    @Override
    public TestCaseExecutionResponse createTestCaseByManual(TestCaseExecutionRequest request) {
        if (request == null || request.getTestCase() == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "Test case request cannot be null");
        }

        TestCaseRequest tcReq = request.getTestCase();
        Feature feature = featureRepository.findByFeatureIdAndIsDeletedFalse(tcReq.getFeatureId())
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with ID: " + tcReq.getFeatureId()));

        if (tcReq.getTestcaseFormatId() != null && !tcReq.getTestcaseFormatId().isBlank()) {
            if (testCaseRepository.existsByFeature_FeatureIdAndTestcaseFormatId(feature.getFeatureId(), tcReq.getTestcaseFormatId())) {
                throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "Test case format ID already exists in this feature: " + tcReq.getTestcaseFormatId());
            }
        }

        User currentUser = authService.getCurrentUser();

        TestCase testCase = testCaseMapper.toEntity(tcReq);
        testCase.setFeature(feature);
        testCase.setCreatedBy(currentUser);
        testCase.setUpdatedBy(currentUser);
        testCase.setActive(true);
        testCase.setDeleted(false);
        if (testCase.getTestcaseStatus() == null) {
            testCase.setTestcaseStatus(TestCaseStatus.NO_RUN);
        }

        TestCase savedTestCase = testCaseRepository.save(testCase);

        TestingExecution execution = null;
        if (request.getTestingExecutionRequest() != null) {
            TestingExecutionRequest execReq = request.getTestingExecutionRequest();
            execution = testingExecutionMapper.toEntity(execReq);
            execution.setTestCase(savedTestCase);
            execution.setExecutedBy(currentUser);
            if (execution.getExecutionNumber() == null) {
                execution.setExecutionNumber(1);
            }
            execution = testingExecutionRepository.save(execution);
        }

        TestCaseResponse tcResponse = testCaseMapper.toResponse(savedTestCase, execution);
        TestingExecutionResponse execResponse = execution != null ? testingExecutionMapper.toResponse(execution) : null;

        return TestCaseExecutionResponse.builder()
                .testCaseResponse(tcResponse)
                .testingExecutionResponse(execResponse)
                .build();
    }

    @Override
    public ExcelUploadResponse createTestCaseByUpload(MultipartFile file, Integer featureId) {
        Feature feature = featureRepository.findByFeatureIdAndIsDeletedFalse(featureId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with ID: " + featureId));

        User currentUser = authService.getCurrentUser();
        List<ExcelTestCaseRow> rows = testCaseExcelService.parseExcel(file);

        List<ExcelUploadErrorResponse> errors = new ArrayList<>();
        int successCount = 0;

        for (ExcelTestCaseRow row : rows) {
            try {
                TestType testType = TestType.UI;
                if (row.getTestType() != null && !row.getTestType().isBlank()) {
                    try {
                        testType = TestType.valueOf(row.getTestType().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        testType = TestType.UI;
                    }
                }

                TestPriority testPriority = TestPriority.MEDIUM;
                if (row.getAutomationPriority() != null && !row.getAutomationPriority().isBlank()) {
                    try {
                        testPriority = TestPriority.valueOf(row.getAutomationPriority().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        testPriority = TestPriority.MEDIUM;
                    }
                }

                TestCaseStatus testCaseStatus = TestCaseStatus.NO_RUN;
                if (row.getActualStatus() != null && !row.getActualStatus().isBlank()) {
                    try {
                        testCaseStatus = TestCaseStatus.valueOf(row.getActualStatus().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        testCaseStatus = TestCaseStatus.NO_RUN;
                    }
                }

                AutomationFeasibility automationFeasibility = AutomationFeasibility.NO;
                if (row.getAutomationStatus() != null && !row.getAutomationStatus().isBlank()) {
                    try {
                        automationFeasibility = AutomationFeasibility.valueOf(row.getAutomationStatus().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        automationFeasibility = AutomationFeasibility.NO;
                    }
                }

                TestCase testCase = TestCase.builder()
                        .feature(feature)
                        .testcaseFormatId(row.getTestcaseFormatId())
                        .title(row.getTitle())
                        .testType(testType)
                        .testPriority(testPriority)
                        .testcaseStatus(testCaseStatus)
                        .dynamicFields(row.getDynamicFields())
                        .createdBy(currentUser)
                        .updatedBy(currentUser)
                        .isActive(true)
                        .isDeleted(false)
                        .build();

                TestCase savedTestCase = testCaseRepository.save(testCase);

                TestingExecution execution = TestingExecution.builder()
                        .testCase(savedTestCase)
                        .testExecution(row.getTestExecution())
                        .testValidation(row.getTestValidation())
                        .precondition(row.getPreCondition())
                        .testData(row.getTestData())
                        .executionSteps(row.getExecutionSteps())
                        .uiValidations(row.getUiValidations())
                        .dbValidations(row.getDbValidations())
                        .automationFeasibility(automationFeasibility)
                        .executionNumber(1)
                        .comments(row.getComments())
                        .executedBy(currentUser)
                        .build();

                testingExecutionRepository.save(execution);
                successCount++;
            } catch (Exception ex) {
                errors.add(ExcelUploadErrorResponse.builder()
                        .row(row.getRowNumber())
                        .column("General")
                        .value(row.getTestcaseFormatId())
                        .message("Failed to process row: " + ex.getMessage())
                        .build());
            }
        }

        return ExcelUploadResponse.builder()
                .message("Excel processing completed")
                .totalRows(rows.size())
                .successRows(successCount)
                .failedRows(errors.size())
                .errors(errors)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadTemplate(Integer projectId, Integer featureId) {
        return excelTemplateService.generateTemplate(projectId, featureId);
    }

    @Override
    public TestCasePutResponse updateTestcaseDetails(TestCasePutRequest testCasePutRequest, Integer id) {
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test case not found with ID: " + id));

        if (!testCase.isActive() || testCase.isDeleted()) {
            throw new IllegalStateException("Cannot update disabled/deleted test case with ID: " + id);
        }

        TestCase updatedTestCase = testCaseMapper.putMethodMapper(testCasePutRequest, testCase);
        updatedTestCase.setUpdatedBy(authService.getCurrentUser());

        if (testCasePutRequest.getDynamicFields() != null) {
            updatedTestCase.setDynamicFields(new HashMap<>(testCasePutRequest.getDynamicFields()));
        }

        testCaseRepository.save(updatedTestCase);

        TestingExecution testingExecution = testingExecutionRepository.findByTestCase(testCase)
                .orElseGet(() -> TestingExecution.builder().testCase(updatedTestCase).build());

        TestingExecution updatedExecution = testingExecutionMapper.putMethodToExecution(testCasePutRequest, testingExecution);
        updatedExecution.setExecutedBy(authService.getCurrentUser());

        testingExecutionRepository.save(updatedExecution);

        return testCaseMapper.toPutResponse(updatedTestCase, updatedExecution);
    }

    @Override
    public String patchTestCaseDetails(TestCasePatchRequest request, Integer id) {
        if (request == null) {
            throw new IllegalArgumentException("Patch request cannot be null");
        }

        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test case not found with ID: " + id));

        if (testCase.isDeleted() || !testCase.isActive()) {
            throw new ResourceNotFoundException("Test case is inactive or deleted with ID: " + id);
        }

        List<String> updatedFields = new ArrayList<>();

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            testCase.setTitle(request.getTitle());
            updatedFields.add("title");
        }

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

        testCase.setUpdatedBy(authService.getCurrentUser());

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

        boolean hasExecutionUpdates = request.getComments() != null
                || request.getAutomationFeasibility() != null
                || request.getExecutionStatus() != null
                || request.getTestExecution() != null
                || request.getTestValidation() != null
                || request.getPrecondition() != null
                || request.getTestData() != null
                || request.getExecutionSteps() != null
                || request.getUiValidations() != null
                || request.getDbValidations() != null;

        if (hasExecutionUpdates) {
            TestingExecution execution = testingExecutionRepository.findByTestCase(testCase)
                    .orElseGet(() -> TestingExecution.builder().testCase(testCase).build());

            if (request.getTestExecution() != null) {
                execution.setTestExecution(request.getTestExecution());
                updatedFields.add("testExecution");
            }
            if (request.getTestValidation() != null) {
                execution.setTestValidation(request.getTestValidation());
                updatedFields.add("testValidation");
            }
            if (request.getPrecondition() != null) {
                execution.setPrecondition(request.getPrecondition());
                updatedFields.add("precondition");
            }
            if (request.getTestData() != null) {
                execution.setTestData(request.getTestData());
                updatedFields.add("testData");
            }
            if (request.getExecutionSteps() != null) {
                execution.setExecutionSteps(request.getExecutionSteps());
                updatedFields.add("executionSteps");
            }
            if (request.getUiValidations() != null) {
                execution.setUiValidations(request.getUiValidations());
                updatedFields.add("uiValidations");
            }
            if (request.getDbValidations() != null) {
                execution.setDbValidations(request.getDbValidations());
                updatedFields.add("dbValidations");
            }
            if (request.getComments() != null) {
                execution.setComments(request.getComments());
                updatedFields.add("comments");
            }
            if (request.getAutomationFeasibility() != null) {
                execution.setAutomationFeasibility(request.getAutomationFeasibility());
                updatedFields.add("automationFeasibility");
            }

            if (request.getExecutionStatus() != null) {
                ExecutionStatus executionStatus = request.getExecutionStatus();
                execution.setExecutionStatus(executionStatus);

                TestCaseStatus testCaseStatus = switch (executionStatus) {
                    case PASS -> TestCaseStatus.PASSED;
                    case FAIL -> TestCaseStatus.FAILED;
                    case DESCOPE -> TestCaseStatus.DESCOPE;
                };

                testCase.setTestcaseStatus(testCaseStatus);
                execution.setExecutionNumber((execution.getExecutionNumber() != null ? execution.getExecutionNumber() : 0) + 1);
                execution.setExecutedAt(Instant.now());
                execution.setExecutedBy(authService.getCurrentUser());
                updatedFields.add("executionStatus");
            }

            testingExecutionRepository.save(execution);
        }

        if (updatedFields.isEmpty()) {
            throw new IllegalArgumentException("At least one field must be provided for update");
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

        if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        int boundedSize = Math.min(pageable.getPageSize(), 100);
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), boundedSize, pageable.getSort());

        Page<TestCase> testCasesPage = testCaseRepository.findByFeatureIdWithFilters(
                featureId, status, type, priority, safePageable);

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
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (testCase.isDeleted()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        if (!includeInactive && !testCase.isActive()) {
            throw new CustomException(ErrorCode.BUSINESS_RULE_VIOLATION);
        }

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
                .orElseThrow(() -> new ResourceNotFoundException("Test case not found with ID: " + id));

        String testcaseFormatId = testCase.getTestcaseFormatId();

        testingExecutionRepository.findByTestCase(testCase)
                .ifPresent(testingExecutionRepository::delete);

        bugRepository.findByTestCase_TestcaseId(id)
                .ifPresent(bugRepository::deleteAll);

        testCaseRepository.delete(testCase);

        return "Test case " + (testcaseFormatId != null ? testcaseFormatId : id) + " deleted successfully";
    }

    @Override
    public PatchTestCaseDeleteResponse softDeleteTestCase(Integer id) {
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test case not found with ID: " + id));

        testCase.setDeleted(true);
        testCase.setActive(false);
        testCase.setUpdatedBy(authService.getCurrentUser());
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
