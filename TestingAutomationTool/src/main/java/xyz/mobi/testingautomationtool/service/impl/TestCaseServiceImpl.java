package xyz.mobi.testingautomationtool.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelParseResult;
import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelTestCaseRow;
import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelUploadErrorResponse;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.TestCasePatchRequest;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.UpdateExecutionStatusRequest;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelUploadResponse;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.TestCasePutRequest;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.ExecutionStatusResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.TestCasePutResponse;
import xyz.mobi.testingautomationtool.entity.*;
import xyz.mobi.testingautomationtool.enums.*;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.patchMapper.TestCasePatchMapper;
import xyz.mobi.testingautomationtool.mapper.postMapper.TestCaseMapper;
import xyz.mobi.testingautomationtool.mapper.putMapper.TestCasePutMapper;
import xyz.mobi.testingautomationtool.repository.*;
import xyz.mobi.testingautomationtool.service.TestCaseExcelService;
import xyz.mobi.testingautomationtool.service.TestCaseService;
import xyz.mobi.testingautomationtool.utils.Utils;

import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@Transactional
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final TestingExecutionRepository testingExecutionRepository;
    private final FeatureRepository featureRepository;
    private final UserRepository userRepository;
    private final BugRepository bugRepository;

    private final TestCaseMapper testCaseMapper;
    private final TestCasePutMapper testCasePutMapper;
    private final TestCasePatchMapper testCasePatchMapper;

    private final TestCaseExcelService testCaseExcelService;

    private final Utils utils;



    @Override
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


    @Override
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
    }

    @Override
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
    }

    @Override
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
    }

    @Override
    public Page<TestCaseExecutionResponse> getByFeatureId(Integer featureId,int page,int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<TestCase> testCases =
                testCaseRepository.findByFeature_FeatureIdAndTestCase_IsActive(
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


    @Override
    public TestCasePutResponse updateTestcaseDetails(TestCasePutRequest testCasePutRequest, 
                                                     Integer id) {
        
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(()->
                        new ResourceNotFoundException(" case is not present with id:"+id));
        
        TestCase updatedTestCase =  testCasePutMapper.putMethodMapper(testCasePutRequest,testCase);
        
        testCaseRepository.save(updatedTestCase);
        
        TestingExecution testingExecution = testingExecutionRepository.findByTestCase(testCase)
                .orElseThrow(()->
                        new ResourceNotFoundException("Execution details are not present in this id:"+id));
        
        TestingExecution updatedExecution = testCasePutMapper.putMethodToExecution(testCasePutRequest, 
                testingExecution);
        
        testingExecutionRepository.save(updatedExecution);
        
        return testCasePutMapper.convertToResponse(updatedTestCase,updatedExecution);
    }


    @Override
    public TestCaseResponse patchTestCaseDetails(TestCasePatchRequest testCasePatchRequest,
                                                 Integer id){

        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(()->
                        new ResourceNotFoundException("Test case is not present"));

        if (testCasePatchRequest.getTestType() == null &&
                testCasePatchRequest.getTestPriority() == null) {

            throw new IllegalArgumentException(
                    "At least one field (testType or testPriority) must be provided"
            );
        }

        if (testCasePatchRequest.getTestType() != null) {
            testCase.setTestType(testCasePatchRequest.getTestType());
        }

        if (testCasePatchRequest.getTestPriority() != null) {
            testCase.setTestPriority(testCasePatchRequest.getTestPriority());
        }

        TestingExecution execution = testingExecutionRepository
                .findByTestCaseTestcaseId(testCase.getTestcaseId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Execution is not present for this test case"));

        execution.setComments(testCasePatchRequest.getComments());

        TestCase updatedTestCase = testCaseRepository.save(testCase);

        return testCasePatchMapper.toResponse(updatedTestCase);
    }

    @Override
    public String softDeleteTestCase(Integer id) {

        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Test case is not present"));

        String testcaseFormatId = testCase.getTestcaseFormatId();

        if (!testCase.isActive()) {
            throw new IllegalArgumentException(
                    "Test case " + testcaseFormatId + " is already disabled"
            );
        }

        testCase.setActive(false);

        List<Bug> bugList = bugRepository.findByTestCase_TestcaseId(id).
                orElseThrow(()->new ResourceNotFoundException("No bugs are available for this testcase"));

        bugList.forEach(bug -> bug.setActive(false));

        bugRepository.saveAll(bugList);

        testCaseRepository.save(testCase);

        return "Test case " + testcaseFormatId + " disabled successfully";
    }

    @Override
    public String hardDeleteTestCase(Integer id) {

        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Test case is not present"));

        String testcaseFormatId = testCase.getTestcaseFormatId();

        testCaseRepository.delete(testCase);

        return "Test case " + testcaseFormatId + " deleted successfully";
    }

    @Override
    public ExecutionStatusResponse updateExecutionStatus(
            Integer testCaseId,
            UpdateExecutionStatusRequest request) {

        TestCase testCase =
                testCaseRepository.findById(testCaseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Testcase not found with ID: " + testCaseId));

        TestingExecution execution =
                testingExecutionRepository.findByTestCase(testCase)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Execution not found with ID: " + testCaseId));;
        
        //1. Update execution status
        execution.setExecutionStatus(request.getExecutionStatus());

        //2. Update TestCase status
        TestCaseStatus testCaseStatus = switch (request.getExecutionStatus()) {
            case PASS -> TestCaseStatus.PASSED;
            case FAIL -> TestCaseStatus.FAILED;
            case DESCOPE -> TestCaseStatus.DESCOPE;
            default -> throw new IllegalArgumentException(
                    "Unsupported execution status: "
                            + request.getExecutionStatus());
        };

        testCase.setTestcaseStatus(testCaseStatus);

        execution.setExecutionNumber(execution.getExecutionNumber() + 1);

//        if(request.getExecutionStatus() == ExecutionStatus.FAIL){
//            execution.setBugsCount(execution.getBugsCount()+1);
//        }

        if(request.getExecutionStatus()==ExecutionStatus.PASS){
            utils.trigger(testCase,testCase.getCreatedBy());
        }

         //3. Save execution + testcase
        TestingExecution updateTestingExecution = testingExecutionRepository.save(execution);
        testCaseRepository.save(testCase);
        return testCasePatchMapper.patchExecutionUpdate(updateTestingExecution);

    }

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
//
}