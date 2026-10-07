package xyz.mobi.testingautomationtool.service.impl;

import org.springframework.cache.annotation.Cacheable;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.ExcelDTO.ExcelTestCaseRow;
import xyz.mobi.testingautomationtool.dto.ExcelDTO.ExcelUploadResponse;
import xyz.mobi.testingautomationtool.dto.TestCaseDTO.*;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.dto.TestingExecutionDTO.TestingExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestingExecutionDTO.TestingExecutionResponse;
import xyz.mobi.testingautomationtool.entity.*;
import xyz.mobi.testingautomationtool.enums.*;
import xyz.mobi.testingautomationtool.exception.*;
import xyz.mobi.testingautomationtool.mapper.TestCaseMapper;
import xyz.mobi.testingautomationtool.mapper.TestingExecutionMapper;
import xyz.mobi.testingautomationtool.repository.*;
import xyz.mobi.testingautomationtool.service.AuthService;
import xyz.mobi.testingautomationtool.service.TestCaseService;
import xyz.mobi.testingautomationtool.specification.TestCaseSpecification;
import xyz.mobi.testingautomationtool.utils.Utils;

import java.lang.Exception;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {
    private final Utils utils;
    private final TestCaseRepository testCaseRepository;
    private final TestingExecutionRepository testingExecutionRepository;
    private final FeatureRepository featureRepository;
    private final BugRepository bugRepository;
    private final TestCaseMapper testCaseMapper;
    private final TestingExecutionMapper testingExecutionMapper;
    private final AuthService authService;
    private final TestCaseExcelService testCaseExcelService;
    private final ExcelTemplateService excelTemplateService;
    private final AttachmentRepository attachmentRepository;

    @Override
    public TestCaseExecutionResponse createTestCaseByManual(TestCaseExecutionRequest request) {
        if (request == null || request.getTestCase() == null) {
            throw new IllegalArgumentException("Test case request cannot be null");
        }

        xyz.mobi.testingautomationtool.dto.TestCaseDTO.TestCaseRequest tcReq = request.getTestCase();
        Feature feature = featureRepository.findByFeatureIdAndIsDeletedFalse(tcReq.getFeatureId())
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with ID: " + tcReq.getFeatureId()));

        if (tcReq.getTestcaseFormatId() != null && !tcReq.getTestcaseFormatId().isBlank()) {
            if (testCaseRepository.existsByFeature_FeatureIdAndTestcaseFormatId(feature.getFeatureId(), tcReq.getTestcaseFormatId())) {
                throw new DuplicateResourceException("Test case format ID already exists in this feature: " + tcReq.getTestcaseFormatId());
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

        if (request.getTestCase().getDynamicFields() != null) {
            testCase.setDynamicFields(request.getTestCase().getDynamicFields());
        } else {
            testCase.setDynamicFields(new HashMap<>());
        }

        TestCase savedTestCase = testCaseRepository.save(testCase);

        TestingExecution execution = null;
        if (request.getTestingExecutionRequest() != null) {
            TestingExecutionRequest execReq = request.getTestingExecutionRequest();
            execution = testingExecutionMapper.toEntity(execReq);
            execution.setTestCase(savedTestCase);
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
    @Transactional
    public ExcelUploadResponse createTestCaseByUpload(
            MultipartFile file, Integer featureId) {

        try {

            List<ExcelTestCaseRow> rows =
                    testCaseExcelService.parseExcel(file);


            Feature feature = featureRepository
                    .findByFeatureIdAndIsDeletedFalse(featureId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Feature not found with ID: " + featureId
                            )
                    );


            User currentUser = authService.getCurrentUser();


            for (ExcelTestCaseRow row : rows) {

                String testcaseFormatId =
                        row.getTestcaseFormatId().trim();

                boolean exists =
                        testCaseRepository
                                .existsByFeatureFeatureIdAndTestcaseFormatId(
                                        featureId,
                                        testcaseFormatId
                                );

                if (exists) {
                    throw new DuplicateResourceException(
                            "Testcase ID already exists for this feature: "
                                    + testcaseFormatId
                    );
                }
            }


            List<TestCase> testCases = new ArrayList<>();

            for (ExcelTestCaseRow row : rows) {

                TestCase testCase = TestCase.builder()
                        .feature(feature)

                        .testcaseFormatId(
                                row.getTestcaseFormatId().trim()
                        )

                        .title(
                                isBlank(row.getTitle())
                                        ? null
                                        : row.getTitle().trim()
                        )

                        .testType(
                                isBlank(row.getTestType())
                                        ? null
                                        : TestType.valueOf(
                                        row.getTestType()
                                        .trim()
                                        .toUpperCase()
                                )
                        )

                        .testPriority(
                                isBlank(row.getAutomationPriority())
                                        ? null
                                        : TestPriority.valueOf(
                                        row.getAutomationPriority()
                                        .trim()
                                        .toUpperCase()
                                )
                        )

                        .testcaseStatus(
                                isBlank(row.getActualStatus())
                                        ? TestCaseStatus.NO_RUN
                                        : TestCaseStatus.valueOf(
                                        row.getActualStatus()
                                        .trim()
                                        .toUpperCase()
                                )
                        )

                        // Additional Excel columns
                        // are stored in JSON
                        .dynamicFields(
                                row.getDynamicFields()
                        )

                        .createdBy(currentUser)
                        .updatedBy(currentUser)

                        .isActive(true)
                        .isDeleted(false)

                        .build();

                testCases.add(testCase);
            }


            // 6. Save all TestCases
            List<TestCase> savedTestCases =
                    testCaseRepository.saveAll(testCases);


            // 7. Create TestingExecution for every TestCase
            List<TestingExecution> executions =
                    new ArrayList<>();

            for (int i = 0; i < savedTestCases.size(); i++) {

                TestCase savedTestCase =
                        savedTestCases.get(i);

                ExcelTestCaseRow row =
                        rows.get(i);

                TestingExecution execution =
                        TestingExecution.builder()

                                .testCase(savedTestCase)

                                .bugsCount(0)

                                .executionNumber(0)

                                .automationFeasibility(
                                        isBlank(
                                                row.getAutomationStatus()
                                        )
                                                ? AutomationFeasibility.YES
                                                : AutomationFeasibility.valueOf(
                                                row.getAutomationStatus()
                                                .trim()
                                                .toUpperCase()
                                        )
                                )

                                .executionStatus(null)

                                .testExecution(
                                        row.getTestExecution()
                                )

                                .testValidation(
                                        row.getTestValidation()
                                )

                                .uiValidations(
                                        row.getUiValidations()
                                )

                                .dbValidations(
                                        row.getDbValidations()
                                )

                                .comments(
                                        row.getComments()
                                )

                                .precondition(
                                        row.getPreCondition()
                                )

                                .executionSteps(
                                        row.getExecutionSteps()
                                )

                                .testData(
                                        row.getTestData()
                                )

                                .executedAt(null)

                                .executedBy(null)

                                .build();

                executions.add(execution);
            }


            // 8. Save all executions
            testingExecutionRepository.saveAll(executions);


            // 9. Save uploaded Excel in Attachment table
            Attachment attachment = Attachment.builder()
                    .attachmentType(AttachmentType.TESTCASE)
                    .feature(feature)
                    .fileBlob(file.getBytes())
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .uploadedBy(currentUser)
                    .build();

            attachmentRepository.save(attachment);


            // 10. Return successful response
            return ExcelUploadResponse.builder()
                    .message("Excel upload successful")
                    .totalRows(rows.size())
                    .successRows(savedTestCases.size())
                    .failedRows(0)
                    .errors(Collections.emptyList())
                    .build();


        } catch (ExcelValidationException |
                 AttachmentProcessingException |
                 DuplicateResourceException |
                 ExcelProcessingException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new ExcelProcessingException(
                    "Failed to upload test cases from Excel"
            );
        }
    }


    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
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
            if (testCase.getDynamicFields() == null) {
                testCase.setDynamicFields(new HashMap<>());
            }
            updatedTestCase.getDynamicFields().putAll(testCasePutRequest.getDynamicFields());
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

        if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters");
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
    @Cacheable(value = "testCases", key = "#id + '-' + #includeInactive")
    public TestCaseResponse getById(Integer id, boolean includeInactive) {
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test case not found with ID: " + id));

        if (testCase.isDeleted()) {
            throw new ResourceNotFoundException("Test case not found with ID: " + id);
        }

        if (!includeInactive && !testCase.isActive()) {
            throw new IllegalStateException("Test case is not active with ID: " + id);
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

        int boundedSize = Math.min(
                pageable.getPageSize() <= 0 ? 10 : pageable.getPageSize(), 100);

        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                boundedSize,
                pageable.getSort());

        Specification<TestCase> spec =
                TestCaseSpecification.search(
                        keyword,
                        featureId,
                        status,
                        type,
                        priority);

        Page<TestCase> testCasesPage =
                testCaseRepository.findAll(spec, safePageable);

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
