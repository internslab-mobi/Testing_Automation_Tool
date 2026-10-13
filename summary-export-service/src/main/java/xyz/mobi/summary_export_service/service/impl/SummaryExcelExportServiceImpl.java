package xyz.mobi.summary_export_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.summary_export_service.dto.BugRowData;
import xyz.mobi.summary_export_service.dto.FeatureSummaryRowData;
import xyz.mobi.summary_export_service.dto.ReportData;
import xyz.mobi.summary_export_service.dto.TestCaseRowData;
import xyz.mobi.summary_export_service.entity.Bug;
import xyz.mobi.summary_export_service.entity.Feature;
import xyz.mobi.summary_export_service.entity.Project;
import xyz.mobi.summary_export_service.entity.TestCase;
import xyz.mobi.summary_export_service.entity.TestingExecution;
import xyz.mobi.summary_export_service.exception.ResourceNotFoundException;
import xyz.mobi.summary_export_service.repository.BugRepository;
import xyz.mobi.summary_export_service.repository.FeatureRepository;
import xyz.mobi.summary_export_service.repository.ProjectRepository;
import xyz.mobi.summary_export_service.repository.TestCaseRepository;
import xyz.mobi.summary_export_service.repository.TestingExecutionRepository;
import xyz.mobi.summary_export_service.service.SummaryExcelExportService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryExcelExportServiceImpl implements SummaryExcelExportService {

    private final ProjectRepository projectRepository;
    private final FeatureRepository featureRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestingExecutionRepository testingExecutionRepository;
    private final BugRepository bugRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String[] TESTCASE_HEADERS = {
            "TestcaseID",
            "Title",
            "Test Type",
            "Test Execution",
            "Test Validation",
            "Automation Priority",
            "Pre Condition",
            "Test data",
            "Execution steps",
            "UI Validations",
            "DB Validations",
            "Automation Status",
            "Actual Status",
            "Comments"
    };

    private static final String[] BUG_HEADERS = {
            "Feature",
            "Bug Format ID",
            "Title",
            "Description",
            "Severity",
            "Priority",
            "Status",
            "Category",
            "Bug Occurrence",
            "Assigned To"
    };

    private static final int[] TESTCASE_COLUMN_WIDTHS = {
            18, 35, 18, 25, 25,
            22, 30, 30, 40, 35,
            35, 22, 22, 35
    };

    private static final int[] BUG_COLUMN_WIDTHS = {
            20, 20, 30, 40, 18,
            18, 18, 20, 18, 20
    };

    @Override
    @Transactional(readOnly = true)
    public byte[] exportProjectSummary(Integer projectId) {
        log.info("Generating QA Summary Report for Project ID: {}", projectId);
        Project project = projectRepository.findByProjectIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        List<Feature> features = featureRepository.findByProjectIdAndIsDeletedFalse(projectId);
        List<Integer> featureIds = features.stream().map(Feature::getFeatureId).toList();

        Map<Integer, String> featureNameMap = features.stream()
                .collect(Collectors.toMap(Feature::getFeatureId, Feature::getFeatureName, (a, b) -> a));

        List<TestCase> testCases = featureIds.isEmpty() ? Collections.emptyList()
                : testCaseRepository.findByFeatureIdInAndIsDeletedFalse(featureIds);

        List<Integer> testCaseIds = testCases.stream().map(TestCase::getTestcaseId).toList();
        List<TestingExecution> executions = testCaseIds.isEmpty() ? Collections.emptyList()
                : testingExecutionRepository.findByTestcaseIdIn(testCaseIds);

        Map<Integer, TestingExecution> executionMap = executions.stream()
                .collect(Collectors.toMap(TestingExecution::getTestcaseId, e -> e, (existing, replacement) ->
                        (replacement.getExecutionId() != null && existing.getExecutionId() != null && replacement.getExecutionId() > existing.getExecutionId())
                                ? replacement : existing));

        List<Bug> bugs = featureIds.isEmpty() ? Collections.emptyList()
                : bugRepository.findByFeatureIdInAndIsDeletedFalse(featureIds);

        ReportData reportData = buildReportData(
                "PROJECT",
                project.getProjectId(),
                project.getProjectName(),
                project.getProjectName(),
                project.getRegion(),
                project.getStatus(),
                project.getDescription(),
                features,
                testCases,
                executionMap,
                bugs,
                featureNameMap
        );

        return generateExcelWorkbook(reportData);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportFeatureSummary(Integer featureId) {
        log.info("Generating QA Summary Report for Feature ID: {}", featureId);
        Feature feature = featureRepository.findByFeatureIdAndIsDeletedFalse(featureId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + featureId));

        Project project = projectRepository.findById(feature.getProjectId()).orElse(null);
        String projectName = project != null ? project.getProjectName() : "N/A";
        String region = project != null ? project.getRegion() : "N/A";

        Map<Integer, String> featureNameMap = Map.of(feature.getFeatureId(), feature.getFeatureName());

        List<TestCase> testCases = testCaseRepository.findByFeatureIdAndIsDeletedFalse(featureId);
        List<Integer> testCaseIds = testCases.stream().map(TestCase::getTestcaseId).toList();

        List<TestingExecution> executions = testCaseIds.isEmpty() ? Collections.emptyList()
                : testingExecutionRepository.findByTestcaseIdIn(testCaseIds);


        Map<Integer, TestingExecution> executionMap = executions.stream()
                .collect(Collectors.toMap(TestingExecution::getTestcaseId, e -> e, (existing, replacement) ->
                        (replacement.getExecutionId() != null && existing.getExecutionId() != null && replacement.getExecutionId() > existing.getExecutionId())
                                ? replacement : existing));

        List<Bug> bugs = bugRepository.findByFeatureIdAndIsDeletedFalse(featureId);

        ReportData reportData = buildReportData(
                "FEATURE",
                feature.getFeatureId(),
                feature.getFeatureName(),
                projectName,
                region,
                feature.getStatus(),
                "Feature scoped QA export",
                Collections.emptyList(),
                testCases,
                executionMap,
                bugs,
                featureNameMap
        );

        return generateExcelWorkbook(reportData);
    }

    private ReportData buildReportData(
            String scope,
            Integer targetId,
            String targetName,
            String projectName,
            String region,
            String status,
            String description,
            List<Feature> features,
            List<TestCase> testCases,
            Map<Integer, TestingExecution> executionMap,
            List<Bug> bugs,
            Map<Integer, String> featureNameMap
    ) {
        int totalTestCases = testCases.size();
        int passedCount = 0;
        int failedCount = 0;
        int blockedCount = 0;
        int noRunCount = 0;

        List<TestCaseRowData> testCaseRows = new ArrayList<>(totalTestCases);

        for (TestCase tc : testCases) {
            TestingExecution exec = executionMap.get(tc.getTestcaseId());
            String execStatus = resolveExecutionStatus(tc, exec);

            switch (execStatus) {
                case "PASS" -> passedCount++;
                case "FAIL" -> failedCount++;
                case "BLOCKED" -> blockedCount++;
                default -> noRunCount++;
            }

            String autoStatus = (exec != null && exec.getAutomationFeasibility() != null)
                    ? exec.getAutomationFeasibility() : "NOT_SPECIFIED";

            String uiVal = (exec != null && exec.getUiValidations() != null) ? exec.getUiValidations() : "N/A";
            String dbVal = (exec != null && exec.getDbValidations() != null) ? exec.getDbValidations() : "N/A";
            java.time.Instant execAt = (exec != null) ? exec.getExecutedAt() : null;

            testCaseRows.add(TestCaseRowData.builder()
                    .testcaseId(tc.getTestcaseId())
                    .testcaseFormatId(tc.getTestcaseFormatId() != null ? tc.getTestcaseFormatId() : "")
                    .featureName(featureNameMap.getOrDefault(tc.getFeatureId(), "Feature #" + tc.getFeatureId()))
                    .title(tc.getTitle() != null ? tc.getTitle() : "")
                    .testType(tc.getTestType() != null ? tc.getTestType() : "")
                    .testExecution(exec != null && exec.getTestExecution() != null ? exec.getTestExecution() : "")
                    .testValidation(exec != null && exec.getTestValidation() != null ? exec.getTestValidation() : "")
                    .automationPriority(tc.getTestPriority() != null ? tc.getTestPriority() : "")
                    .testPriority(tc.getTestPriority() != null ? tc.getTestPriority() : "")
                    .precondition(exec != null && exec.getPrecondition() != null ? exec.getPrecondition() : "")
                    .testData(exec != null && exec.getTestData() != null ? exec.getTestData() : "")
                    .executionSteps(exec != null && exec.getExecutionSteps() != null ? exec.getExecutionSteps() : "")
                    .uiValidations(exec != null && exec.getUiValidations() != null ? exec.getUiValidations() : "")
                    .dbValidations(exec != null && exec.getDbValidations() != null ? exec.getDbValidations() : "")
                    .uiValidation(uiVal)
                    .dbValidation(dbVal)
                    .automationStatus(autoStatus)
                    .actualStatus(tc.getTestcaseStatus() != null ? tc.getTestcaseStatus() : execStatus)
                    .executionStatus(execStatus)
                    .comments(exec != null && exec.getComments() != null ? exec.getComments() : "")
                    .executedAt(execAt)
                    .build());
        }

        int executedCount = passedCount + failedCount + blockedCount;
        double executionRate = totalTestCases > 0 ? ((double) executedCount / totalTestCases) * 100.0 : 0.0;
        double passRate = executedCount > 0 ? ((double) passedCount / executedCount) * 100.0 : 0.0;

        int totalDefects = bugs.size();
        int criticalDefects = 0;
        int openDefects = 0;

        List<BugRowData> bugRows = new ArrayList<>(totalDefects);

        for (Bug bug : bugs) {
            String severity = bug.getSeverity() != null ? bug.getSeverity().name() : "MEDIUM";
            String priority = bug.getPriority() != null ? bug.getPriority().name() : "MEDIUM";
            String bugStatus = bug.getStatus() != null ? bug.getStatus().name() : "OPEN";

            if (severity.equals("CRITICAL") || severity.equals("BLOCKER")) {
                criticalDefects++;
            }

            if (!bugStatus.equals("CLOSED") && !bugStatus.equals("RESOLVED")
                    && !bugStatus.equals("FIXED") && !bugStatus.equals("VERIFIED")) {
                openDefects++;
            }

            String assignedToStr = "";
            if (bug.getAssignedTo() != null && bug.getAssignedTo().getUserId() != null) {
                assignedToStr = String.valueOf(bug.getAssignedTo().getUserId());
            }

            bugRows.add(BugRowData.builder()
                    .featureName(featureNameMap.getOrDefault(bug.getFeatureId(), "Feature #" + bug.getFeatureId()))
                    .bugFormatId(bug.getBugFormatId() != null ? bug.getBugFormatId() : "BUG-" + bug.getBugId())
                    .title(bug.getTitle() != null ? bug.getTitle() : "")
                    .description(bug.getDescription() != null ? bug.getDescription() : "")
                    .severity(severity)
                    .priority(priority)
                    .status(bugStatus)
                    .category(bug.getCategory() != null ? bug.getCategory().name() : "")
                    .bugOccurrence(bug.getBugOccurrence() != null ? bug.getBugOccurrence() : 0)
                    .assignedTo(assignedToStr)
                    .rcaComments(bug.getRcaComments() != null && !bug.getRcaComments().isBlank()
                            ? bug.getRcaComments() : "None provided")
                    .build());
        }

        int closedDefects = totalDefects - openDefects;
        double defectFixRate = totalDefects > 0 ? ((double) closedDefects / totalDefects) * 100.0 : 0.0;

        // Quality Health Score formula: 100 - (FailedTestCases * 5) - (CriticalBugs * 15)
        int healthScore = 100 - (failedCount * 5) - (criticalDefects * 15);
        String rating;
        if (healthScore >= 85) {
            rating = "EXCELLENT (Release Ready)";
        } else if (healthScore >= 70) {
            rating = "GOOD (Acceptable Quality)";
        } else if (healthScore >= 50) {
            rating = "NEEDS ATTENTION (Elevated Risk)";
        } else {
            rating = "CRITICAL RISK (Sign-Off Blocked)";
        }

        List<FeatureSummaryRowData> featureRows = new ArrayList<>();
        if (features != null) {
            for (Feature f : features) {
                int fTotal = 0;
                int fPassed = 0;
                int fFailed = 0;
                for (TestCase tc : testCases) {
                    if (tc.getFeatureId() != null && tc.getFeatureId().equals(f.getFeatureId())) {
                        fTotal++;
                        TestingExecution exec = executionMap.get(tc.getTestcaseId());
                        String st = resolveExecutionStatus(tc, exec);
                        if ("PASS".equals(st)) fPassed++;
                        else if ("FAIL".equals(st)) fFailed++;
                    }
                }
                int fBugCount = 0;
                for (Bug b : bugs) {
                    if (b.getFeatureId() != null && b.getFeatureId().equals(f.getFeatureId())) {
                        fBugCount++;
                    }
                }
                featureRows.add(FeatureSummaryRowData.builder()
                        .featureId(f.getFeatureId())
                        .featureName(f.getFeatureName())
                        .status(f.getStatus() != null ? f.getStatus() : "ACTIVE")
                        .totalTestCases(fTotal)
                        .passedCount(fPassed)
                        .failedCount(fFailed)
                        .bugCount(fBugCount)
                        .build());
            }
        }

        return ReportData.builder()
                .reportScope(scope)
                .targetId(targetId)
                .targetName(targetName)
                .projectName(projectName)
                .region(region != null ? region : "N/A")
                .status(status != null ? status : "ACTIVE")
                .description(description != null ? description : "")
                .exportDate(LocalDateTime.now().format(DATE_TIME_FORMATTER))
                .totalTestCases(totalTestCases)
                .executedCount(executedCount)
                .executionRate(executionRate)
                .passedCount(passedCount)
                .failedCount(failedCount)
                .noRunCount(noRunCount)
                .blockedCount(blockedCount)
                .passRate(passRate)
                .totalDefects(totalDefects)
                .criticalDefects(criticalDefects)
                .openDefects(openDefects)
                .closedDefects(closedDefects)
                .defectFixRate(defectFixRate)
                .qualityHealthScore(healthScore)
                .healthRating(rating)
                .testCaseRows(testCaseRows)
                .bugRows(bugRows)
                .featureRows(featureRows)
                .build();
    }

    private String resolveExecutionStatus(TestCase tc, TestingExecution exec) {
        String raw = null;
        if (exec != null && exec.getExecutionStatus() != null && !exec.getExecutionStatus().isBlank()) {
            raw = exec.getExecutionStatus().trim().toUpperCase();
        } else if (tc.getTestcaseStatus() != null && !tc.getTestcaseStatus().isBlank()) {
            raw = tc.getTestcaseStatus().trim().toUpperCase();
        }

        if (raw == null) {
            return "NO_RUN";
        }

        if (raw.contains("PASS")) {
            return "PASS";
        }
        if (raw.contains("FAIL")) {
            return "FAIL";
        }
        if (raw.contains("BLOCK")) {
            return "BLOCKED";
        }
        return "NO_RUN";
    }

    private byte[] generateExcelWorkbook(ReportData data) {
        // SXSSFWorkbook with row window 100 ensures streaming low-memory operation
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            workbook.setCompressTempFiles(true);

            // Create styles
            StylesHolder styles = createStyles(workbook);

            // Sheet 1: Executive Summary
            createExecutiveSummarySheet(workbook, data, styles);

            // Sheet 2: Testcase Execution Summary
            createTestCaseExecutionSheet(workbook, data, styles);

            // Sheet 3: Defect RCA Breakdown
            createDefectRcaSheet(workbook, data, styles);

            workbook.write(out);
            workbook.dispose(); // clean up SXSSF temp disk files
            log.info("Successfully generated summary workbook for {} ID: {}", data.getReportScope(), data.getTargetId());
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate summary Excel workbook: {}", e.getMessage(), e);
            throw new RuntimeException("Error occurred while generating Excel report: " + e.getMessage(), e);
        }
    }

    private void createExecutiveSummarySheet(SXSSFWorkbook workbook, ReportData data, StylesHolder styles) {
        SXSSFSheet sheet = workbook.createSheet("Executive_Summary");
        sheet.setDisplayGridlines(true);
        sheet.setPrintGridlines(true);

        sheet.setColumnWidth(0, 32 * 256);
        sheet.setColumnWidth(1, 28 * 256);
        sheet.setColumnWidth(2, 24 * 256);
        sheet.setColumnWidth(3, 36 * 256);

        int rowIndex = 0;

        // Banner Title
        Row titleRow = sheet.createRow(rowIndex++);
        titleRow.setHeightInPoints(34);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("QA EXECUTIVE SUMMARY REPORT — " + data.getTargetName().toUpperCase());
        titleCell.setCellStyle(styles.titleBannerStyle);
        for (int c = 1; c <= 3; c++) {
            Cell emptyCell = titleRow.createCell(c);
            emptyCell.setCellStyle(styles.titleBannerStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        rowIndex++; // Blank spacer

        // Section 1: Scope & Metadata
        Row metaHeaderRow = sheet.createRow(rowIndex++);
        metaHeaderRow.setHeightInPoints(24);
        createSectionHeader(metaHeaderRow, "1. SCOPE & METADATA OVERVIEW", styles.sectionHeaderStyle, 3, sheet);

        addMetaRow(sheet, rowIndex++, "Report Scope", data.getReportScope(), "Export Timestamp", data.getExportDate(), styles);
        addMetaRow(sheet, rowIndex++, "Target Entity Name", data.getTargetName(), "Target Entity ID", String.valueOf(data.getTargetId()), styles);
        addMetaRow(sheet, rowIndex++, "Parent Project", data.getProjectName(), "Target Region", data.getRegion(), styles);
        addMetaRow(sheet, rowIndex++, "Lifecycle Status", data.getStatus(), "Generated By", "TAT Summary Export Microservice", styles);

        rowIndex++; // Blank spacer

        // Section 2: Test Execution KPIs
        Row kpiHeaderRow = sheet.createRow(rowIndex++);
        kpiHeaderRow.setHeightInPoints(24);
        createSectionHeader(kpiHeaderRow, "2. TEST EXECUTION LIFECYCLE KPIS", styles.sectionHeaderStyle, 3, sheet);

        Row kpiColHeader = sheet.createRow(rowIndex++);
        kpiColHeader.setHeightInPoints(22);
        createStyledCell(kpiColHeader, 0, "Execution Metric", styles.tableHeaderStyle);
        createStyledCell(kpiColHeader, 1, "Count", styles.tableHeaderStyle);
        createStyledCell(kpiColHeader, 2, "Percentage", styles.tableHeaderStyle);
        createStyledCell(kpiColHeader, 3, "Quality Benchmark", styles.tableHeaderStyle);

        addKpiRow(sheet, rowIndex++, "Total Test Cases", data.getTotalTestCases(), 100.0, "Total Authored Cases", styles, null);
        addKpiRow(sheet, rowIndex++, "Executed Cases (Run)", data.getExecutedCount(), data.getExecutionRate(), "Target: >= 90.0%", styles, null);
        addKpiRow(sheet, rowIndex++, "Passed Test Cases", data.getPassedCount(), data.getPassRate(), "Pass Rate (of Executed)", styles, styles.passStatusStyle);
        addKpiRow(sheet, rowIndex++, "Failed Test Cases", data.getFailedCount(), data.getExecutedCount() > 0 ? ((double) data.getFailedCount() / data.getExecutedCount()) * 100.0 : 0.0, "Target: 0 Failures", styles, styles.failStatusStyle);
        addKpiRow(sheet, rowIndex++, "No Run / Untested", data.getNoRunCount(), data.getTotalTestCases() > 0 ? ((double) data.getNoRunCount() / data.getTotalTestCases()) * 100.0 : 0.0, "Cases Pending Execution", styles, styles.noRunStatusStyle);
        addKpiRow(sheet, rowIndex++, "Blocked Cases", data.getBlockedCount(), data.getTotalTestCases() > 0 ? ((double) data.getBlockedCount() / data.getTotalTestCases()) * 100.0 : 0.0, "Blocked / Descope", styles, styles.noRunStatusStyle);

        rowIndex++; // Blank spacer

        // Section 3: Defect & RCA Breakdown
        Row defectHeaderRow = sheet.createRow(rowIndex++);
        defectHeaderRow.setHeightInPoints(24);
        createSectionHeader(defectHeaderRow, "3. DEFECT & ISSUE MANAGEMENT OVERVIEW", styles.sectionHeaderStyle, 3, sheet);

        Row defectColHeader = sheet.createRow(rowIndex++);
        defectColHeader.setHeightInPoints(22);
        createStyledCell(defectColHeader, 0, "Defect Category", styles.tableHeaderStyle);
        createStyledCell(defectColHeader, 1, "Count", styles.tableHeaderStyle);
        createStyledCell(defectColHeader, 2, "Distribution Rate", styles.tableHeaderStyle);
        createStyledCell(defectColHeader, 3, "Description & Impact", styles.tableHeaderStyle);

        addKpiRow(sheet, rowIndex++, "Total Defects Logged", data.getTotalDefects(), 100.0, "All Bugs Tracked", styles, null);
        addKpiRow(sheet, rowIndex++, "Critical / Blocker Defects", data.getCriticalDefects(), data.getTotalDefects() > 0 ? ((double) data.getCriticalDefects() / data.getTotalDefects()) * 100.0 : 0.0, "High Severity - Release Inhibitors", styles, styles.criticalSeverityStyle);
        addKpiRow(sheet, rowIndex++, "Open / Active Defects", data.getOpenDefects(), data.getTotalDefects() > 0 ? ((double) data.getOpenDefects() / data.getTotalDefects()) * 100.0 : 0.0, "Pending Resolution", styles, styles.highSeverityStyle);
        addKpiRow(sheet, rowIndex++, "Closed / Resolved Defects", data.getClosedDefects(), data.getDefectFixRate(), "Fix & Verification Rate", styles, styles.passStatusStyle);

        rowIndex++; // Blank spacer

        // Section 4: Quality Health Score
        Row healthHeaderRow = sheet.createRow(rowIndex++);
        healthHeaderRow.setHeightInPoints(24);
        createSectionHeader(healthHeaderRow, "4. QUALITY HEALTH SCORE & RELEASE READINESS", styles.sectionHeaderStyle, 3, sheet);

        Row scoreRow = sheet.createRow(rowIndex++);
        scoreRow.setHeightInPoints(32);
        createStyledCell(scoreRow, 0, "Quality Health Score", styles.metaLabelStyle);
        Cell scoreCell = scoreRow.createCell(1);
        scoreCell.setCellValue(data.getQualityHealthScore());
        scoreCell.setCellStyle(data.getQualityHealthScore() >= 70 ? styles.healthScoreGoodStyle : styles.healthScoreBadStyle);

        createStyledCell(scoreRow, 2, "Health Rating", styles.metaLabelStyle);
        Cell ratingCell = scoreRow.createCell(3);
        ratingCell.setCellValue(data.getHealthRating());
        ratingCell.setCellStyle(data.getQualityHealthScore() >= 70 ? styles.passStatusStyle : styles.failStatusStyle);

        Row formulaRow = sheet.createRow(rowIndex++);
        formulaRow.setHeightInPoints(22);
        createStyledCell(formulaRow, 0, "Formula Applied", styles.metaLabelStyle);
        Cell formulaCell = formulaRow.createCell(1);
        formulaCell.setCellValue("100 - (FailedTestCases * 5) - (CriticalBugs * 15)");
        formulaCell.setCellStyle(styles.regularStyle);

        createStyledCell(formulaRow, 2, "Calculation Breakdown", styles.metaLabelStyle);
        Cell calcCell = formulaRow.createCell(3);
        calcCell.setCellValue(String.format("100 - (%d * 5) - (%d * 15) = %d pts",
                data.getFailedCount(), data.getCriticalDefects(), data.getQualityHealthScore()));
        calcCell.setCellStyle(styles.regularStyle);

        // Section 5: Feature Overview / Name List (Only displayed for Project scope)
        if ("PROJECT".equalsIgnoreCase(data.getReportScope()) && data.getFeatureRows() != null && !data.getFeatureRows().isEmpty()) {
            rowIndex++; // Blank spacer
            Row fSectionRow = sheet.createRow(rowIndex++);
            fSectionRow.setHeightInPoints(24);
            createSectionHeader(fSectionRow, "5. PROJECT FEATURES LIST & PROGRESS", styles.sectionHeaderStyle, 3, sheet);

            Row fHeaderRow = sheet.createRow(rowIndex++);
            fHeaderRow.setHeightInPoints(22);
            createStyledCell(fHeaderRow, 0, "Feature Name", styles.tableHeaderStyle);
            createStyledCell(fHeaderRow, 1, "Status", styles.tableHeaderStyle);
            createStyledCell(fHeaderRow, 2, "Test Cases (Pass / Fail / Total)", styles.tableHeaderStyle);
            createStyledCell(fHeaderRow, 3, "Defects Logged", styles.tableHeaderStyle);

            for (FeatureSummaryRowData fRow : data.getFeatureRows()) {
                Row r = sheet.createRow(rowIndex++);
                r.setHeightInPoints(20);
                createStyledCell(r, 0, fRow.getFeatureName(), styles.regularStyle);
                createStyledCell(r, 1, fRow.getStatus(), styles.centeredStyle);
                String summary = String.format("%d Pass / %d Fail / %d Total",
                        fRow.getPassedCount(), fRow.getFailedCount(), fRow.getTotalTestCases());
                createStyledCell(r, 2, summary, styles.centeredStyle);
                createStyledCell(r, 3, fRow.getBugCount() + " Bugs", styles.centeredStyle);
            }
        }
    }

    private void createTestCaseExecutionSheet(SXSSFWorkbook workbook, ReportData data, StylesHolder styles) {
        SXSSFSheet sheet = workbook.createSheet("Testcase_Execution_Summary");
        sheet.setDisplayGridlines(true);
        sheet.setPrintGridlines(true);

        for (int i = 0; i < TESTCASE_COLUMN_WIDTHS.length; i++) {
            sheet.setColumnWidth(i, TESTCASE_COLUMN_WIDTHS[i] * 256);
        }

        int rowIndex = 0;

        // Title row
        Row titleRow = sheet.createRow(rowIndex++);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("TEST CASE EXECUTION DETAIL REPORT (" + data.getTestCaseRows().size() + " CASES)");
        titleCell.setCellStyle(styles.titleBannerStyle);
        for (int c = 1; c < TESTCASE_HEADERS.length; c++) {
            Cell empty = titleRow.createCell(c);
            empty.setCellStyle(styles.titleBannerStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, TESTCASE_HEADERS.length - 1));

        // Column Headers
        Row headerRow = sheet.createRow(rowIndex++);
        headerRow.setHeightInPoints(24);
        for (int i = 0; i < TESTCASE_HEADERS.length; i++) {
            createStyledCell(headerRow, i, TESTCASE_HEADERS[i], styles.tableHeaderStyle);
        }

        // Data Rows
        for (TestCaseRowData rowData : data.getTestCaseRows()) {
            Row r = sheet.createRow(rowIndex++);
            r.setHeightInPoints(20);

            // 0. TestcaseID
            String tcIdDisplay = rowData.getTestcaseFormatId() != null && !rowData.getTestcaseFormatId().isBlank()
                    ? rowData.getTestcaseFormatId() : ("TC-" + rowData.getTestcaseId());
            createStyledCell(r, 0, tcIdDisplay, styles.centeredStyle);

            // 1. Title
            createStyledCell(r, 1, rowData.getTitle() != null ? rowData.getTitle() : "", styles.regularStyle);

            // 2. Test Type
            createStyledCell(r, 2, rowData.getTestType() != null ? rowData.getTestType() : "", styles.centeredStyle);

            // 3. Test Execution
            createStyledCell(r, 3, rowData.getTestExecution() != null ? rowData.getTestExecution() : "", styles.regularStyle);

            // 4. Test Validation
            createStyledCell(r, 4, rowData.getTestValidation() != null ? rowData.getTestValidation() : "", styles.regularStyle);

            // 5. Automation Priority
            createStyledCell(r, 5, rowData.getAutomationPriority() != null ? rowData.getAutomationPriority() : "", styles.centeredStyle);

            // 6. Pre Condition
            createStyledCell(r, 6, rowData.getPrecondition() != null ? rowData.getPrecondition() : "", styles.regularStyle);

            // 7. Test data
            createStyledCell(r, 7, rowData.getTestData() != null ? rowData.getTestData() : "", styles.regularStyle);

            // 8. Execution steps
            createStyledCell(r, 8, rowData.getExecutionSteps() != null ? rowData.getExecutionSteps() : "", styles.regularStyle);

            // 9. UI Validations
            createStyledCell(r, 9, rowData.getUiValidations() != null ? rowData.getUiValidations() : "", styles.regularStyle);

            // 10. DB Validations
            createStyledCell(r, 10, rowData.getDbValidations() != null ? rowData.getDbValidations() : "", styles.regularStyle);

            // 11. Automation Status
            createStyledCell(r, 11, rowData.getAutomationStatus() != null ? rowData.getAutomationStatus() : "", styles.centeredStyle);

            // 12. Actual Status
            CellStyle statusStyle = switch (rowData.getActualStatus()) {
                case "PASS" -> styles.passStatusStyle;
                case "FAIL" -> styles.failStatusStyle;
                case "BLOCKED" -> styles.noRunStatusStyle;
                default -> styles.centeredStyle;
            };
            createStyledCell(r, 12, rowData.getActualStatus() != null ? rowData.getActualStatus() : "", statusStyle);

            // 13. Comments
            createStyledCell(r, 13, rowData.getComments() != null ? rowData.getComments() : "", styles.regularStyle);
        }

        sheet.createFreezePane(0, 2);
    }

    private void createDefectRcaSheet(SXSSFWorkbook workbook, ReportData data, StylesHolder styles) {
        SXSSFSheet sheet = workbook.createSheet("Defect_RCA_Breakdown");
        sheet.setDisplayGridlines(true);
        sheet.setPrintGridlines(true);

        for (int i = 0; i < BUG_COLUMN_WIDTHS.length; i++) {
            sheet.setColumnWidth(i, BUG_COLUMN_WIDTHS[i] * 256);
        }

        int rowIndex = 0;

        // Title row
        Row titleRow = sheet.createRow(rowIndex++);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DEFECT & ROOT CAUSE ANALYSIS (RCA) BREAKDOWN (" + data.getBugRows().size() + " DEFECTS)");
        titleCell.setCellStyle(styles.titleBannerStyle);
        for (int c = 1; c < BUG_HEADERS.length; c++) {
            Cell empty = titleRow.createCell(c);
            empty.setCellStyle(styles.titleBannerStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, BUG_HEADERS.length - 1));

        // Column Headers
        Row headerRow = sheet.createRow(rowIndex++);
        headerRow.setHeightInPoints(24);
        for (int i = 0; i < BUG_HEADERS.length; i++) {
            createStyledCell(headerRow, i, BUG_HEADERS[i], styles.tableHeaderStyle);
        }

        // Data Rows
        for (BugRowData bug : data.getBugRows()) {
            Row r = sheet.createRow(rowIndex++);
            r.setHeightInPoints(20);

            // 0. Feature
            createStyledCell(r, 0, bug.getFeatureName() != null ? bug.getFeatureName() : "", styles.regularStyle);

            // 1. Bug Format ID
            createStyledCell(r, 1, bug.getBugFormatId() != null ? bug.getBugFormatId() : "", styles.centeredStyle);

            // 2. Title
            createStyledCell(r, 2, bug.getTitle() != null ? bug.getTitle() : "", styles.regularStyle);

            // 3. Description
            createStyledCell(r, 3, bug.getDescription() != null ? bug.getDescription() : "", styles.regularStyle);

            // 4. Severity
            CellStyle severityStyle = switch (bug.getSeverity() != null ? bug.getSeverity() : "") {
                case "CRITICAL", "BLOCKER" -> styles.criticalSeverityStyle;
                case "HIGH" -> styles.highSeverityStyle;
                default -> styles.centeredStyle;
            };
            createStyledCell(r, 4, bug.getSeverity() != null ? bug.getSeverity() : "", severityStyle);

            // 5. Priority
            createStyledCell(r, 5, bug.getPriority() != null ? bug.getPriority() : "", styles.centeredStyle);

            // 6. Status
            createStyledCell(r, 6, bug.getStatus() != null ? bug.getStatus() : "", styles.centeredStyle);

            // 7. Category
            createStyledCell(r, 7, bug.getCategory() != null ? bug.getCategory() : "", styles.centeredStyle);

            // 8. Bug Occurrence
            createStyledCell(r, 8, bug.getBugOccurrence() != null ? String.valueOf(bug.getBugOccurrence()) : "0", styles.centeredStyle);

            // 9. Assigned To
            createStyledCell(r, 9, bug.getAssignedTo() != null ? bug.getAssignedTo() : "", styles.centeredStyle);
        }

        sheet.createFreezePane(0, 2);
    }

    private void addMetaRow(
            SXSSFSheet sheet,
            int rowIndex,
            String label1,
            String val1,
            String label2,
            String val2,
            StylesHolder styles
    ) {
        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(20);
        createStyledCell(row, 0, label1, styles.metaLabelStyle);
        createStyledCell(row, 1, val1, styles.metaValueStyle);
        createStyledCell(row, 2, label2, styles.metaLabelStyle);
        createStyledCell(row, 3, val2, styles.metaValueStyle);
    }

    private void addKpiRow(
            SXSSFSheet sheet,
            int rowIndex,
            String metric,
            int count,
            double percent,
            String desc,
            StylesHolder styles,
            CellStyle countStyleOverride
    ) {
        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(20);
        createStyledCell(row, 0, metric, styles.regularStyle);

        Cell countCell = row.createCell(1);
        countCell.setCellValue(count);
        countCell.setCellStyle(countStyleOverride != null ? countStyleOverride : styles.numberStyle);

        Cell pctCell = row.createCell(2);
        pctCell.setCellValue(String.format(Locale.US, "%.1f%%", percent));
        pctCell.setCellStyle(styles.centeredStyle);

        createStyledCell(row, 3, desc, styles.regularStyle);
    }

    private void createSectionHeader(Row row, String title, CellStyle style, int maxCol, SXSSFSheet sheet) {
        for (int c = 0; c <= maxCol; c++) {
            Cell cell = row.createCell(c);
            cell.setCellValue(c == 0 ? title : "");
            cell.setCellStyle(style);
        }
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, maxCol));
    }

    private void createStyledCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private StylesHolder createStyles(SXSSFWorkbook wb) {
        StylesHolder h = new StylesHolder();

        // Fonts
        Font titleFont = wb.createFont();
        titleFont.setFontName("Calibri");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBold(true);
        titleFont.setColor(IndexedColors.WHITE.getIndex());

        Font sectionFont = wb.createFont();
        sectionFont.setFontName("Calibri");
        sectionFont.setFontHeightInPoints((short) 11);
        sectionFont.setBold(true);
        sectionFont.setColor(IndexedColors.WHITE.getIndex());

        Font headerFont = wb.createFont();
        headerFont.setFontName("Calibri");
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        Font boldFont = wb.createFont();
        boldFont.setFontName("Calibri");
        boldFont.setFontHeightInPoints((short) 10);
        boldFont.setBold(true);

        Font regularFont = wb.createFont();
        regularFont.setFontName("Calibri");
        regularFont.setFontHeightInPoints((short) 10);

        Font passFont = wb.createFont();
        passFont.setFontName("Calibri");
        passFont.setFontHeightInPoints((short) 10);
        passFont.setBold(true);
        passFont.setColor(IndexedColors.DARK_GREEN.getIndex());

        Font failFont = wb.createFont();
        failFont.setFontName("Calibri");
        failFont.setFontHeightInPoints((short) 10);
        failFont.setBold(true);
        failFont.setColor(IndexedColors.RED.getIndex());

        Font criticalFont = wb.createFont();
        criticalFont.setFontName("Calibri");
        criticalFont.setFontHeightInPoints((short) 10);
        criticalFont.setBold(true);
        criticalFont.setColor(IndexedColors.RED.getIndex());

        Font highFont = wb.createFont();
        highFont.setFontName("Calibri");
        highFont.setFontHeightInPoints((short) 10);
        highFont.setBold(true);
        highFont.setColor(IndexedColors.DARK_YELLOW.getIndex());

        Font healthScoreFont = wb.createFont();
        healthScoreFont.setFontName("Calibri");
        healthScoreFont.setFontHeightInPoints((short) 16);
        healthScoreFont.setBold(true);

        // 1. Title Banner Style
        h.titleBannerStyle = wb.createCellStyle();
        h.titleBannerStyle.setFont(titleFont);
        h.titleBannerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        h.titleBannerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        h.titleBannerStyle.setAlignment(HorizontalAlignment.LEFT);
        h.titleBannerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 2. Section Header Style
        h.sectionHeaderStyle = wb.createCellStyle();
        h.sectionHeaderStyle.setFont(sectionFont);
        h.sectionHeaderStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        h.sectionHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        h.sectionHeaderStyle.setAlignment(HorizontalAlignment.LEFT);
        h.sectionHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 3. Table Column Header Style
        h.tableHeaderStyle = wb.createCellStyle();
        h.tableHeaderStyle.setFont(headerFont);
        h.tableHeaderStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        h.tableHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        h.tableHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        h.tableHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(h.tableHeaderStyle);

        // 4. Meta Label Style
        h.metaLabelStyle = wb.createCellStyle();
        h.metaLabelStyle.setFont(boldFont);
        h.metaLabelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        h.metaLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        h.metaLabelStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(h.metaLabelStyle);

        // 5. Meta Value Style
        h.metaValueStyle = wb.createCellStyle();
        h.metaValueStyle.setFont(regularFont);
        h.metaValueStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(h.metaValueStyle);

        // 6. Regular Data Style
        h.regularStyle = wb.createCellStyle();
        h.regularStyle.setFont(regularFont);
        h.regularStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(h.regularStyle);

        // 7. Centered Data Style
        h.centeredStyle = wb.createCellStyle();
        h.centeredStyle.setFont(regularFont);
        h.centeredStyle.setAlignment(HorizontalAlignment.CENTER);
        h.centeredStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(h.centeredStyle);

        // 8. Number Style
        h.numberStyle = wb.createCellStyle();
        h.numberStyle.setFont(boldFont);
        h.numberStyle.setAlignment(HorizontalAlignment.CENTER);
        h.numberStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(h.numberStyle);

        // 9. Pass Status Style (Green)
        h.passStatusStyle = wb.createCellStyle();
        h.passStatusStyle.setFont(passFont);
        h.passStatusStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        h.passStatusStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        h.passStatusStyle.setAlignment(HorizontalAlignment.CENTER);
        h.passStatusStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(h.passStatusStyle);

        // 10. Fail Status Style (Red)
        h.failStatusStyle = wb.createCellStyle();
        h.failStatusStyle.setFont(failFont);
        h.failStatusStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        h.failStatusStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        h.failStatusStyle.setAlignment(HorizontalAlignment.CENTER);
        h.failStatusStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(h.failStatusStyle);

        // 11. No Run / Blocked Style (Yellow/Grey)
        h.noRunStatusStyle = wb.createCellStyle();
        h.noRunStatusStyle.setFont(boldFont);
        h.noRunStatusStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        h.noRunStatusStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        h.noRunStatusStyle.setAlignment(HorizontalAlignment.CENTER);
        h.noRunStatusStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(h.noRunStatusStyle);

        // 12. Critical Severity Style (Red fill)
        h.criticalSeverityStyle = wb.createCellStyle();
        h.criticalSeverityStyle.setFont(criticalFont);
        h.criticalSeverityStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        h.criticalSeverityStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        h.criticalSeverityStyle.setAlignment(HorizontalAlignment.CENTER);
        h.criticalSeverityStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(h.criticalSeverityStyle);

        // 13. High Severity Style (Orange fill)
        h.highSeverityStyle = wb.createCellStyle();
        h.highSeverityStyle.setFont(highFont);
        h.highSeverityStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        h.highSeverityStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        h.highSeverityStyle.setAlignment(HorizontalAlignment.CENTER);
        h.highSeverityStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(h.highSeverityStyle);

        // 14. Health Score Styles
        h.healthScoreGoodStyle = wb.createCellStyle();
        Font fg = wb.createFont();
        fg.setFontName("Calibri");
        fg.setFontHeightInPoints((short) 16);
        fg.setBold(true);
        fg.setColor(IndexedColors.DARK_GREEN.getIndex());
        h.healthScoreGoodStyle.setFont(fg);
        h.healthScoreGoodStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        h.healthScoreGoodStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        h.healthScoreGoodStyle.setAlignment(HorizontalAlignment.CENTER);
        h.healthScoreGoodStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(h.healthScoreGoodStyle);

        h.healthScoreBadStyle = wb.createCellStyle();
        Font fb = wb.createFont();
        fb.setFontName("Calibri");
        fb.setFontHeightInPoints((short) 16);
        fb.setBold(true);
        fb.setColor(IndexedColors.RED.getIndex());
        h.healthScoreBadStyle.setFont(fb);
        h.healthScoreBadStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        h.healthScoreBadStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        h.healthScoreBadStyle.setAlignment(HorizontalAlignment.CENTER);
        h.healthScoreBadStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(h.healthScoreBadStyle);

        return h;
    }

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private static class StylesHolder {
        CellStyle titleBannerStyle;
        CellStyle sectionHeaderStyle;
        CellStyle tableHeaderStyle;
        CellStyle metaLabelStyle;
        CellStyle metaValueStyle;
        CellStyle regularStyle;
        CellStyle centeredStyle;
        CellStyle numberStyle;
        CellStyle passStatusStyle;
        CellStyle failStatusStyle;
        CellStyle noRunStatusStyle;
        CellStyle criticalSeverityStyle;
        CellStyle highSeverityStyle;
        CellStyle healthScoreGoodStyle;
        CellStyle healthScoreBadStyle;
    }
}
