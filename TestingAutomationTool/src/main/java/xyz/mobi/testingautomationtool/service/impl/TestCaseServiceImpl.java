package xyz.mobi.testingautomationtool.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
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
import xyz.mobi.testingautomationtool.utils.Utils;

import java.util.HashMap;


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
    public PatchTestCaseResponse patchTestCaseDetails(
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
        boolean hasUpdate =
                request.getTestType() != null ||
                        request.getTestPriority() != null ||
                        request.getIsActive() != null ||
                        request.getComments() != null ||
                        request.getDynamicFields() != null ||
                        request.getExecutionStatus() != null ||
                        request.getAutomationFeasibility() != null;

        if (!hasUpdate) {
            throw new IllegalArgumentException(
                    "At least one field must be provided for update");
        }


        if (request.getTestType() != null) {
            testCase.setTestType(request.getTestType());
        }


        if (request.getTestPriority() != null) {
            testCase.setTestPriority(request.getTestPriority());
        }

        if (request.getIsActive() != null) {

            boolean active = request.getIsActive();

            testCase.setActive(active);
            if (!active) {
                bugRepository.deactivateBugs(id);
            } else {
                bugRepository.activateNonDeletedBugs(id);
            }
        }

        // 6. Update soft-delete status


        //After user creation the value is need to change by the login user


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
        }

        // 9. Find execution details
        TestingExecution execution =
                testingExecutionRepository
                        .findByTestCase(testCase)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Execution details not found for test case ID: "
                                                + id));

        // 10. Update comments
        if (request.getComments() != null) {
            execution.setComments(request.getComments());
        }

        // 11. Update automation feasibility
        if (request.getAutomationFeasibility() != null) {

            execution.setAutomationFeasibility(
                    request.getAutomationFeasibility());
        }

        // 12. Update execution status
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
        }

        testingExecutionRepository.save(execution);

        TestCase savedTestCase =
                testCaseRepository.save(testCase);
        PatchTestCaseResponse response = PatchTestCaseResponse.builder()
                .testcaseId(savedTestCase.getTestcaseId())
                .featureId(savedTestCase.getFeature().getFeatureId())
                .testcaseFormatId(savedTestCase.getTestcaseFormatId())
                .updatedAt(savedTestCase.getUpdatedAt())
                .build();
        if (request.getTestType() != null) {
            response.setTestType(savedTestCase.getTestType());
        }

        if (request.getTestPriority() != null) {
            response.setTestPriority(savedTestCase.getTestPriority());
        }

        if (request.getIsActive() != null) {
            response.setIsActive(savedTestCase.isActive());
        }

        if (request.getDynamicFields() != null) {
            response.setDynamicFields(savedTestCase.getDynamicFields());
        }

        if (request.getComments() != null
                || request.getExecutionStatus() != null
                || request.getAutomationFeasibility() != null) {

            PatchExecutionResponse patchExecutionResponse =
                    PatchExecutionResponse.builder()
                            .build();

            if (request.getComments() != null) {
                patchExecutionResponse.setComments(
                        request.getComments());
            }

            if (request.getExecutionStatus() != null) {
                patchExecutionResponse.setExecutionStatus(
                        request.getExecutionStatus());
            }

            if (request.getAutomationFeasibility() != null) {
                patchExecutionResponse.setAutomationFeasibility(
                        request.getAutomationFeasibility());
            }

            response.setExecution(patchExecutionResponse);


        }
        return response;
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
    }

    @Override
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
}



    /*    *//*@Override
    public TestCaseExecutionResponse createTestCaseByManual(
            TestCaseExecutionRequest request) {

        String formatId = request.getTestCase().getTestcaseFormatId();

        if (testCaseRepository.existsByTestcaseFormatId(formatId)) {
                throw new IllegalArgumentException(
                    "Test case format ID already exists: " + formatId
            );
        }

        TestCase testCase = testCaseMapper.toEntity(request.getTestCase());

//        System.out.println("STATUS AFTER MAPPER = " + testCase.getTestcaseStatus());

        Feature feature = featureRepository.findById(
                request.getTestCase().getFeatureId()
        ).orElseThrow(() -> new RuntimeException("Feature not found"));

        testCase.setFeature(feature);

        //current user
//        User createdBy = userRepository.findById(
//                request.getTestCase().getCreatedBy()
//        ).orElseThrow(() ->
//                new RuntimeException("Created by user not found")
//        );
//
//        User currentUser = getCurrentUser();

        User dummyUser = userRepository.findById(1)
                .orElseThrow(() ->
                        new RuntimeException("Dummy user not found")
                );

        testCase.setCreatedBy(dummyUser);

        testCase = testCaseRepository.save(testCase);



        TestingExecution execution = testCaseMapper.toEntity(request.getExecutionRequest());

        execution.setTestCase(testCase);

        execution = testingExecutionRepository.save(execution);

        return TestCaseExecutionResponse.builder()
                .testCaseResponse(testCaseMapper.toResponse(testCase))
                .executionResponse(testCaseMapper.toResponse(execution))
                .build();
    }
*//*

    *//*@Override
    @Transactional
    public ExcelUploadResponse createTestCaseByUpload(
            MultipartFile file,
            Integer featureId) {

        // 1. Parse and validate Excel
        ExcelParseResult result =
                testCaseExcelService.parseAndValidate(file, featureId);

        // 2. If Excel validation failed, save nothing
        if (!result.getErrors().isEmpty()) {

            return ExcelUploadResponse.builder()
                    .message("Excel upload failed")
                    .totalRows(result.getRows().size())
                    .successRows(0)
                    .failedRows(result.getErrors().size())
                    .errors(result.getErrors())
                    .build();
        }

        // 3. Find feature
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() ->
                        new RuntimeException("Feature not found with ID: " + featureId));

        // 4. Check duplicate TestcaseFormatId
        for (ExcelTestCaseRow row : result.getRows()) {

            if (testCaseRepository
                    .existsByFeature_FeatureIdAndTestcaseFormatId(
                            featureId, row.getTestcaseFormatId()
                    )) {

                ExcelUploadErrorResponse error =
                        ExcelUploadErrorResponse.builder()
                                .row(row.getRowNumber())
                                .column("TestcaseID")
                                .value(row.getTestcaseFormatId())
                                .message(
                                        "TestcaseID already exists for this feature"
                                )
                                .build();

                result.getErrors().add(error);
            }
        }

        // 5. If DB duplicate found, save NOTHING
        if (!result.getErrors().isEmpty()) {

            return ExcelUploadResponse.builder()
                    .message("Excel upload failed")
                    .totalRows(result.getRows().size())
                    .successRows(0)
                    .failedRows(result.getErrors().size())
                    .errors(result.getErrors())
                    .build();
        }

        // 6. Everything is valid.
        for (ExcelTestCaseRow row : result.getRows()) {

            // Create TestCase

            TestCase testCase = new TestCase();

            testCase.setFeature(feature);

            // Excel TestcaseID → testcaseFormatId
            testCase.setTestcaseFormatId(row.getTestcaseFormatId());

            testCase.setTitle(row.getTitle());

            testCase.setTestType(
                    TestType.valueOf(
                            row.getTestType()
                                    .trim()
                                    .toUpperCase()
                    )
            );

            testCase.setTestPriority(
                    TestPriority.valueOf(
                            row.getAutomationPriority()
                                    .trim()
                                    .toUpperCase()
                    )
            );

            // Default = NO_RUN
            if (!isBlank(row.getActualStatus())) {

                testCase.setTestcaseStatus(
                        TestCaseStatus.valueOf(
                                row.getActualStatus()
                                        .trim()
                                        .toUpperCase()
                        )
                );
            }

            User dummyUser = userRepository.findById(1)
                    .orElseThrow(() ->
                            new RuntimeException("Dummy user not found")
                    );

            testCase.setCreatedBy(dummyUser);

            // DB testcase_id is generated automatically
            testCase = testCaseRepository.save(testCase);

            // Create TestingExecution
            TestingExecution execution = new TestingExecution();

            execution.setTestCase(testCase);

            // Excel Automation Status
            if (!isBlank(row.getAutomationStatus())) {

                execution.setAutomationFeasibility(
                        AutomationFeasibility.valueOf(
                                row.getAutomationStatus()
                                        .trim()
                                        .toUpperCase()
                        )
                );

            } else {
                execution.setAutomationFeasibility(AutomationFeasibility.PENDING);
            }

            execution.setTestExecution(row.getTestExecution());

            execution.setTestValidation(row.getTestValidation());

            execution.setPrecondition(row.getPreCondition());

            execution.setTestData(row.getTestData());

            execution.setExecutionSteps(row.getExecutionSteps());

            execution.setUiValidations(row.getUiValidations());

            execution.setDbValidations(row.getDbValidations());

            execution.setComments(row.getComments());

            testingExecutionRepository.save(execution);
        }

        // 7. Everything saved successfully
        return ExcelUploadResponse.builder()
                .message("Excel uploaded successfully")
                .totalRows(result.getRows().size())
                .successRows(result.getRows().size())
                .failedRows(0)
                .errors(Collections.emptyList())
                .build();
    }*//*

    *//*@Override
    public TestCaseExecutionResponse getById(int id) {

        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Test case not found with ID: " + id));

        TestingExecution execution =
                testingExecutionRepository.findByTestCase(testCase)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Execution not found for test case ID: " + id));

        if(!testCase.isActive()){
            throw  new RuntimeException("The testcase has been removed");
        }

        return TestCaseExecutionResponse.builder()
                .testCaseResponse(testCaseMapper.toResponse(testCase))
                .executionResponse(testCaseMapper.toResponse(execution))
                .build();
    }*//*

    *//*@Override
    public List<TestCaseExecutionResponse> getByAll() {

        List<TestCase> testCases = testCaseRepository.findAll();

        return testCases.stream()
                .map(testCase -> {

                    TestingExecution execution =
                            testingExecutionRepository.findByTestCase(testCase)
                                    .orElse(null);

                    return TestCaseExecutionResponse.builder()
                            .testCaseResponse(
                                    testCaseMapper.toResponse(testCase))
                            .executionResponse(
                                    execution != null
                                            ? testCaseMapper.toResponse(execution)
                                            : null)
                            .build();
                })
                .toList();
    }*//*

   *//* @Override
    public Page<TestCaseExecutionResponse> getByFeatureId(Integer featureId,int page,int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<TestCase> testCases =
                testCaseRepository.findByFeature_FeatureIdAndActiveTrue(
                        featureId,
                        pageable);

        return testCases
                .map(testCase -> {

                    TestingExecution execution =
                            testingExecutionRepository
                                    .findByTestCase(testCase)
                                    .orElse(null);

                    return TestCaseExecutionResponse.builder()
                            .testCaseResponse(
                                    testCaseMapper.toResponse(testCase))
                            .executionResponse(
                                    execution != null
                                            ? testCaseMapper.toResponse(execution)
                                            : null)
                            .build();
                });
    }
*//*



//    private String generateBugFormatId(Integer testcaseId) {
//
//        Optional<Bug> latestBug =
//                bugRepository.findTopByTestCase_TestcaseIdOrderByBugIdDesc(
//                        testcaseId);
//
//        int nextNumber = latestBug
//                .map(bug -> {
//                    String bugFormatId = bug.getBugFormatId();
//
//                    String numberPart =
//                            bugFormatId.substring(
//                                    bugFormatId.lastIndexOf("-") + 1);
//
//                    return Integer.parseInt(numberPart) + 1;
//                })
//                .orElse(1);
//
//        return String.format("BUG-%03d", nextNumber);
//    }
//*/
