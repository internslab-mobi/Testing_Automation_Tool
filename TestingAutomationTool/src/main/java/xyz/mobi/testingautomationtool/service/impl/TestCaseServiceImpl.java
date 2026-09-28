package xyz.mobi.testingautomationtool.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelTestCaseRow;
import xyz.mobi.testingautomationtool.dto.excelDTO.ExcelUploadResponse;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.entity.*;
import xyz.mobi.testingautomationtool.enums.*;
import xyz.mobi.testingautomationtool.exception.*;
import xyz.mobi.testingautomationtool.mapper.postMapper.TestCaseMapper;
import xyz.mobi.testingautomationtool.repository.*;

import xyz.mobi.testingautomationtool.service.TestCaseService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final TestingExecutionRepository testingExecutionRepository;
    private final FeatureRepository featureRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;

    private final TestCaseMapper testCaseMapper;

    private final TestCaseExcelService testCaseExcelService;

    @Override
    public TestCaseExecutionResponse createTestCaseByManual(
            TestCaseExecutionRequest request) {

        String formatId = request.getTestCase().getTestcaseFormatId();
        Integer featureId = request.getTestCase().getFeatureId();

        if (testCaseRepository.existsByFeatureFeatureIdAndTestcaseFormatId(
                featureId, formatId)) {
                throw new DuplicateResourceException(
                    "Test case format ID already exists: " + formatId
            );
        }

        Feature feature = featureRepository.findById(
                featureId
        ).orElseThrow(() -> new ResourceNotFoundException(
                "Feature Id : "+ featureId +" not found"));


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
                        new RuntimeException("User not found")
                );


        TestCase testCase = testCaseMapper.toEntity(request.getTestCase());

        testCase.setFeature(feature);
        testCase.setCreatedBy(dummyUser);

        if (request.getTestCase().getDynamicFields() != null) {
            testCase.setDynamicFields(request.getTestCase().getDynamicFields());
        } else {
            testCase.setDynamicFields(new HashMap<>());
        }

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
    public ExcelUploadResponse createTestCaseByUpload(
            MultipartFile file, Integer featureId) {

        try {

            // 1. Parse and validate the complete Excel file
            List<ExcelTestCaseRow> rows = testCaseExcelService.parseExcel(file);

            // 2. Get feature
            Feature feature = featureRepository.findById(featureId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Feature not found with id: " + featureId
                            )
                    );

            // 3. Temporary logged-in user
            User createdBy = userRepository.findById(1)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found with id: 1"
                            )
                    );

            for (ExcelTestCaseRow row : rows) {

                boolean exists =
                        testCaseRepository
                                .existsByFeatureFeatureIdAndTestcaseFormatId(
                                        featureId,
                                        row.getTestcaseFormatId().trim()
                                );

                if (exists) {
                    throw new DuplicateResourceException(
                            "Testcase ID already exists for this feature: "
                                    + row.getTestcaseFormatId()
                    );

                }
            }


            List<TestCase> testCases = new ArrayList<>();

            // 4. Convert Excel rows into TestCase entities
            for (ExcelTestCaseRow row : rows) {

                TestCase testCase = TestCase.builder()
                        .feature(feature)
                        .testcaseFormatId(row.getTestcaseFormatId())
                        .title(isBlank(row.getTitle())
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
                        // Dynamic Excel columns
                        .dynamicFields(row.getDynamicFields())
                        .createdBy(createdBy)
                        .build();
                testCases.add(testCase);
            }

            // 5. Save all test cases
            List<TestCase> savedTestCases = testCaseRepository.saveAll(testCases);

            List<TestingExecution> executions = new ArrayList<>();

            for (int i = 0; i < savedTestCases.size(); i++) {

                TestCase savedTestCase = savedTestCases.get(i);

                ExcelTestCaseRow row = rows.get(i);

                TestingExecution execution = TestingExecution.builder()
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

            testingExecutionRepository.saveAll(executions);

            Attachment attachment = Attachment.builder()
                    .attachmentType(AttachmentType.FEATURE)
                    .feature(feature)
                    .fileBlob(file.getBytes())
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .uploadedBy(createdBy)
                    .build();

            attachmentRepository.save(attachment);

            // 6. Build response
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
                    "Failed to upload test cases from Excel");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }


}