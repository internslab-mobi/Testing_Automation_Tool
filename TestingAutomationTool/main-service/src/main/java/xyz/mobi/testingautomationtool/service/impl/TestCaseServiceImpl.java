package xyz.mobi.testingautomationtool.service.impl;

import xyz.mobi.testingautomationtool.client.SummaryExportClient;

import org.springframework.cache.annotation.Cacheable;
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

import lombok.extern.slf4j.Slf4j;
import java.lang.Exception;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
        private final AttachmentRepository attachmentRepository;
        private final SummaryExportClient summaryExportClient;
        private final AuditLogRepository auditLogRepository;
        private final UserRepository userRepository;

        @Override
        public TestCaseExecutionResponse createTestCaseByManual(TestCaseExecutionRequest request) {
                if (request == null || request.getTestCase() == null) {
                        throw new IllegalArgumentException("Test case request cannot be null");
                }

                xyz.mobi.testingautomationtool.dto.TestCaseDTO.TestCaseRequest tcReq = request.getTestCase();
                Feature feature = featureRepository.findByFeatureIdAndIsDeletedFalse(tcReq.getFeatureId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Feature not found with ID: " + tcReq.getFeatureId()));

                if (tcReq.getTestcaseFormatId() != null && !tcReq.getTestcaseFormatId().isBlank()) {
                        if (testCaseRepository.existsByFeature_FeatureIdAndTestcaseFormatId(feature.getFeatureId(),
                                        tcReq.getTestcaseFormatId())) {
                                throw new DuplicateResourceException(
                                                "Test case format ID already exists in this feature: "
                                                                + tcReq.getTestcaseFormatId());
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
                TestingExecutionResponse execResponse = execution != null ? testingExecutionMapper.toResponse(execution)
                                : null;

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

                        List<ExcelTestCaseRow> rows = testCaseExcelService.parseExcel(file);

                        Feature feature = featureRepository
                                        .findByFeatureIdAndIsDeletedFalse(featureId)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Feature not found with ID: " + featureId));

                        User currentUser = authService.getCurrentUser();

                        for (ExcelTestCaseRow row : rows) {

                                String testcaseFormatId = row.getTestcaseFormatId().trim();

                                boolean exists = testCaseRepository
                                                .existsByFeatureFeatureIdAndTestcaseFormatId(
                                                                featureId,
                                                                testcaseFormatId);

                                if (exists) {
                                        throw new DuplicateResourceException(
                                                        "Testcase ID already exists for this feature: "
                                                                        + testcaseFormatId);
                                }
                        }

                        List<TestCase> testCases = new ArrayList<>();

                        for (ExcelTestCaseRow row : rows) {

                                TestCase testCase = TestCase.builder()
                                                .feature(feature)

                                                .testcaseFormatId(
                                                                row.getTestcaseFormatId().trim())

                                                .title(
                                                                isBlank(row.getTitle())
                                                                                ? null
                                                                                : row.getTitle().trim())

                                                .testType(
                                                                isBlank(row.getTestType())
                                                                                ? null
                                                                                : TestType.valueOf(
                                                                                                row.getTestType()
                                                                                                                .trim()
                                                                                                                .toUpperCase()))

                                                .testPriority(
                                                                isBlank(row.getAutomationPriority())
                                                                                ? null
                                                                                : TestPriority.valueOf(
                                                                                                row.getAutomationPriority()
                                                                                                                .trim()
                                                                                                                .toUpperCase()))

                                                .testcaseStatus(
                                                                isBlank(row.getActualStatus())
                                                                                ? TestCaseStatus.NO_RUN
                                                                                : TestCaseStatus.valueOf(
                                                                                                row.getActualStatus()
                                                                                                                .trim()
                                                                                                                .toUpperCase()))

                                                // Additional Excel columns
                                                // are stored in JSON
                                                .dynamicFields(
                                                                row.getDynamicFields())

                                                .createdBy(currentUser)
                                                .updatedBy(currentUser)

                                                .isActive(true)
                                                .isDeleted(false)

                                                .build();

                                testCases.add(testCase);
                        }

                        // 6. Save all TestCases
                        List<TestCase> savedTestCases = testCaseRepository.saveAll(testCases);

                        // 7. Create TestingExecution for every TestCase
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
                                                                                row.getAutomationStatus())
                                                                                                ? AutomationFeasibility.YES
                                                                                                : AutomationFeasibility
                                                                                                                .valueOf(
                                                                                                                                row.getAutomationStatus()
                                                                                                                                                .trim()
                                                                                                                                                .toUpperCase()))

                                                .executionStatus(null)

                                                .testExecution(
                                                                row.getTestExecution())

                                                .testValidation(
                                                                row.getTestValidation())

                                                .uiValidations(
                                                                row.getUiValidations())

                                                .dbValidations(
                                                                row.getDbValidations())

                                                .comments(
                                                                row.getComments())

                                                .precondition(
                                                                row.getPreCondition())

                                                .executionSteps(
                                                                row.getExecutionSteps())

                                                .testData(
                                                                row.getTestData())

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

                } catch (ExcelValidationException | AttachmentProcessingException | DuplicateResourceException
                                | ExcelProcessingException ex) {

                        throw ex;

                } catch (Exception ex) {

                        throw new ExcelProcessingException(
                                        "Failed to upload test cases from Excel");
                }
        }

        private boolean isBlank(String value) {
                return value == null || value.trim().isBlank();
        }

        @Override
        @Transactional(readOnly = true)
        public byte[] downloadTemplate(Integer featureId) {
                Feature feature = featureRepository.findByFeatureIdAndIsDeletedFalse(featureId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Feature not found with ID: " + featureId));
                Integer projectId = feature.getProject() != null ? feature.getProject().getProjectId() : null;
                if (projectId == null) {
                        throw new ResourceNotFoundException("Project not associated with feature ID: " + featureId);
                }
                return summaryExportClient.downloadTestCaseTemplate(projectId, featureId);
        }

        @Override
        @Transactional(readOnly = true)
        public byte[] downloadBlankTemplate() {
                try {
                        return summaryExportClient.downloadBlankTemplate();
                } catch (Exception e) {
                        log.warn("summary-export-service not reachable for blank template: {}. Generating fallback Excel template.", e.getMessage());
                        return generateBlankTemplateFallback();
                }
        }

        private byte[] generateBlankTemplateFallback() {
                try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                     java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Testcase");
                        sheet.setDisplayGridlines(true);

                        org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
                        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                        font.setBold(true);
                        font.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
                        headerStyle.setFont(font);
                        headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
                        headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
                        headerStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);

                        String[] headers = {
                                        "TestcaseID", "Title", "Test Type", "Test Execution", "Test Validation",
                                        "Automation Priority", "Pre Condition", "Test data", "Execution steps",
                                        "UI Validations", "DB Validations", "Automation Status", "Actual Status", "Comments"
                        };

                        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
                        headerRow.setHeightInPoints(28);
                        for (int i = 0; i < headers.length; i++) {
                                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                                cell.setCellValue(headers[i]);
                                cell.setCellStyle(headerStyle);
                                sheet.setColumnWidth(i, 22 * 256);
                        }

                        // Add sample format row
                        org.apache.poi.ss.usermodel.Row sampleRow = sheet.createRow(1);
                        sampleRow.createCell(0).setCellValue("TC-001");
                        sampleRow.createCell(1).setCellValue("Verify user login with valid credentials");
                        sampleRow.createCell(2).setCellValue("UI");
                        sampleRow.createCell(3).setCellValue("Browser Automated / Manual");
                        sampleRow.createCell(4).setCellValue("Verify successful redirection to dashboard");
                        sampleRow.createCell(5).setCellValue("HIGH");
                        sampleRow.createCell(6).setCellValue("User is registered and active in system");
                        sampleRow.createCell(7).setCellValue("username=testuser@example.com, password=ValidPass123!");
                        sampleRow.createCell(8).setCellValue("1. Go to /login\n2. Enter valid credentials\n3. Click Submit");
                        sampleRow.createCell(9).setCellValue("User avatar and dashboard metrics displayed");
                        sampleRow.createCell(10).setCellValue("User last_login timestamp updated in database");
                        sampleRow.createCell(11).setCellValue("YES");
                        sampleRow.createCell(12).setCellValue("PASS");
                        sampleRow.createCell(13).setCellValue("Sample test case row format");

                        org.apache.poi.ss.usermodel.DataValidationHelper helper = sheet.getDataValidationHelper();
                        addDropdownValidation(sheet, helper, 2, new String[]{"UI", "API", "DB", "E2E"});
                        addDropdownValidation(sheet, helper, 5, new String[]{"HIGH", "MEDIUM", "LOW"});
                        addDropdownValidation(sheet, helper, 11, new String[]{"YES", "NO", "PARTIALLY_FEASIBLE"});
                        addDropdownValidation(sheet, helper, 12, new String[]{"PASS", "FAIL", "BLOCK", "DESCOPE", "IN_PROGRESS", "NO_RUN"});

                        sheet.createFreezePane(0, 1);
                        workbook.write(out);
                        return out.toByteArray();
                } catch (Exception e) {
                        throw new ExcelProcessingException("Failed to generate blank test case template: " + e.getMessage(), e);
                }
        }

        private void addDropdownValidation(org.apache.poi.ss.usermodel.Sheet sheet,
                                           org.apache.poi.ss.usermodel.DataValidationHelper helper,
                                           int colIndex,
                                           String[] values) {
                org.apache.poi.ss.util.CellRangeAddressList addressList = new org.apache.poi.ss.util.CellRangeAddressList(1, 1000, colIndex, colIndex);
                org.apache.poi.ss.usermodel.DataValidationConstraint constraint = helper.createExplicitListConstraint(values);
                org.apache.poi.ss.usermodel.DataValidation validation = helper.createValidation(constraint, addressList);
                validation.setSuppressDropDownArrow(true);
                validation.setShowErrorBox(true);
                sheet.addValidationData(validation);
        }

        @Override
        public TestCaseResponse updateTestcaseDetails(TestCasePutRequest testCasePutRequest, Integer id) {
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

                TestingExecution updatedExecution = testingExecutionMapper.putMethodToExecution(testCasePutRequest,
                                testingExecution);
                updatedExecution.setExecutedBy(authService.getCurrentUser());

                testingExecutionRepository.save(updatedExecution);

                return testCaseMapper.toResponse(updatedTestCase, updatedExecution);
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
                                        case IN_PROGRESS -> TestCaseStatus.IN_PROGRESS;
                                        case BLOCK -> TestCaseStatus.BLOCKED;
                                        default -> throw new IllegalArgumentException(
                                                        "Unsupported execution status: "
                                                                        + executionStatus);
                                };

                                testCase.setTestcaseStatus(testCaseStatus);

                                execution.setExecutionNumber(
                                                execution.getExecutionNumber() + 1);

                                User execUser = authService.getCurrentUser();
                                if (execUser == null) {
                                        execUser = testCase.getCreatedBy();
                                }
                                utils.trigger(
                                                testCase,
                                                execUser,
                                                null);
                                updatedFields.add("executionStatus");
                        }

                        testingExecutionRepository.save(execution);
                }

                if (updatedFields.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "At least one field must be provided for update");
                }

                testCaseRepository.save(testCase);
                return "Test case with ID " + id + " updated successfully. Changed fields: "
                                + String.join(", ", updatedFields);
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

                Page<TestCase> testCasesPage = testCaseRepository.findByFeatureIdWithFilters(
                                featureId, status, type, priority, pageable);

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
        public String hardDeleteTestCase(Integer id) {
                TestCase testCase = testCaseRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Test case not found with ID: " + id));

                String testcaseFormatId = testCase.getTestcaseFormatId();

                testingExecutionRepository.findByTestCase(testCase)
                                .ifPresent(testingExecutionRepository::delete);

                bugRepository.deleteAll(
                                bugRepository.findByTestCase_TestcaseId(id));

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
        public List<Integer> bulkDeleteTestCases(List<Integer> testCaseIds) {
                if (testCaseIds == null || testCaseIds.isEmpty()) {
                        return Collections.emptyList();
                }
                User currentUser = authService.getCurrentUser();
                List<Integer> deletedIds = new ArrayList<>();
                for (Integer id : testCaseIds) {
                        Optional<TestCase> optionalTestCase = testCaseRepository.findById(id);
                        if (optionalTestCase.isPresent()) {
                                TestCase testCase = optionalTestCase.get();
                                if (!testCase.isDeleted()) {
                                        testCase.setDeleted(true);
                                        testCase.setActive(false);
                                        testCase.setUpdatedBy(currentUser);
                                        bugRepository.softDeleteBugsByTestCaseId(id);
                                        testCaseRepository.save(testCase);
                                        deletedIds.add(id);
                                }
                        }
                }
                return deletedIds;
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

                if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0) {
                        throw new IllegalArgumentException("Invalid pagination parameters");
                }

                Specification<TestCase> spec = TestCaseSpecification.search(
                                keyword,
                                featureId,
                                status,
                                type,
                                priority);

                Page<TestCase> testCasesPage = testCaseRepository.findAll(spec, pageable);

                return mapTestCasesWithExecutions(testCasesPage);
        }

        private Page<TestCaseResponse> mapTestCasesWithExecutions(Page<TestCase> testCasesPage) {
                List<Integer> testCaseIds = testCasesPage.getContent().stream()
                                .map(TestCase::getTestcaseId)
                                .toList();

                Map<Integer, TestingExecution> executionMap = Collections.emptyMap();
                if (!testCaseIds.isEmpty()) {
                        List<TestingExecution> executions = testingExecutionRepository
                                        .findByTestCaseTestcaseIdIn(testCaseIds);
                        executionMap = executions.stream()
                                        .collect(Collectors.toMap(
                                                        e -> e.getTestCase().getTestcaseId(),
                                                        e -> e,
                                                        (existing, replacement) -> existing));
                }

                final Map<Integer, TestingExecution> finalExecutionMap = executionMap;
                return testCasesPage.map(testCase -> testCaseMapper.toResponse(testCase,
                                finalExecutionMap.get(testCase.getTestcaseId())));
        }

        @Override
        @Transactional(readOnly = true)
        public List<TestCaseAuditLogResponse> getTestCaseHistory(Integer testcaseId) {
                TestCase testCase = testCaseRepository.findById(testcaseId)
                                .orElseThrow(() -> new ResourceNotFoundException("Test case not found with ID: " + testcaseId));

                List<TestingAuditLog> logs = auditLogRepository.findByTestCase_TestcaseIdOrderByExecutedAtAsc(testcaseId);

                List<TestCaseAuditLogResponse> responses = new java.util.ArrayList<>();
                String runningPreviousStatus = null;

                if (logs == null || logs.isEmpty()) {
                        String creatorName = testCase.getCreatedBy() != null ? testCase.getCreatedBy().getUsername() : "QA Tester";
                        if (testCase.getCreatedBy() != null && testCase.getCreatedBy().getFullName() != null && !testCase.getCreatedBy().getFullName().isBlank()) {
                                creatorName = testCase.getCreatedBy().getFullName();
                        }

                        responses.add(TestCaseAuditLogResponse.builder()
                                        .logId(0)
                                        .testcaseId(testCase.getTestcaseId())
                                        .testcaseFormatId(testCase.getTestcaseFormatId())
                                        .status(testCase.getTestcaseStatus())
                                        .previousStatus(null)
                                        .executedBy(testCase.getCreatedBy() != null ? testCase.getCreatedBy().getUserId() : null)
                                        .executedByName(creatorName)
                                        .executedAt(testCase.getCreatedAt() != null ? testCase.getCreatedAt() : java.time.Instant.now())
                                        .bugId(null)
                                        .bugFormatId(null)
                                        .build());
                        return responses;
                }

                java.util.Map<Integer, String> userNames = new java.util.HashMap<>();

                for (int i = 0; i < logs.size(); i++) {
                        TestingAuditLog logItem = logs.get(i);

                        String execName = null;
                        if (logItem.getExecutedBy() != null) {
                                execName = userNames.computeIfAbsent(logItem.getExecutedBy(), id ->
                                                userRepository.findById(id)
                                                                .map(u -> (u.getFullName() != null && !u.getFullName().isBlank()) ? u.getFullName() : u.getUsername())
                                                                .orElse("User #" + id)
                                );
                        }

                        String currentStatusStr = logItem.getTestcaseStatus() != null ? logItem.getTestcaseStatus().name() : null;

                        Integer bugId = logItem.getBug() != null ? logItem.getBug().getBugId() : null;
                        String bugFormatId = logItem.getBug() != null ? logItem.getBug().getBugFormatId() : null;

                        responses.add(TestCaseAuditLogResponse.builder()
                                        .logId(logItem.getLogId())
                                        .testcaseId(testCase.getTestcaseId())
                                        .testcaseFormatId(testCase.getTestcaseFormatId())
                                        .status(logItem.getTestcaseStatus())
                                        .previousStatus(runningPreviousStatus)
                                        .executedBy(logItem.getExecutedBy())
                                        .executedByName(execName != null ? execName : (i == 0 ? "Tester" : "System"))
                                        .executedAt(logItem.getExecutedAt())
                                        .bugId(bugId)
                                        .bugFormatId(bugFormatId)
                                        .build());

                        runningPreviousStatus = currentStatusStr;
                }

                return responses;
        }
}
