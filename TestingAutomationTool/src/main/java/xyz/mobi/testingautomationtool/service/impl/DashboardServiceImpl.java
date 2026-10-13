package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.DashboardDTO.*;
import xyz.mobi.testingautomationtool.dto.ProjectDTO.ProjectResponse;
import xyz.mobi.testingautomationtool.entity.*;
import xyz.mobi.testingautomationtool.entity.Role;
import xyz.mobi.testingautomationtool.enums.*;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.repository.*;
import xyz.mobi.testingautomationtool.service.DashboardService;
import xyz.mobi.testingautomationtool.service.EmailService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final ProjectRepository projectRepository;
    private final FeatureRepository featureRepository;
    private final TestCaseRepository testCaseRepository;
    private final BugRepository bugRepository;

    @Override
    public PatchResponseForManager userConfirmation(PatchRequestOfManager request) {
        if (request.getRole() == null || request.getUserId() == null) {
            throw new IllegalArgumentException("UserId and Role are required");
        }

        User user = userRepository.findByUserIdAndIsActiveFalse(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found or is already active with ID: " + request.getUserId()));

        Role role = roleRepository.findByRole(String.valueOf(request.getRole()))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Role: " + request.getRole()));

        user.setActive(true);
        user.setRole(role);
        userRepository.save(user);

        emailService.confirmationEmail(user.getEmail(), user.getUsername());

        return PatchResponseForManager.builder()
                .message("User has been confirmed and activated successfully")
                .username(user.getUsername())
                .build();
    }

    @Override
    public String userRejection(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        emailService.rejectEmail(user.getEmail(), user.getUsername());
        userRepository.delete(user);

        return "User has been removed successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public List<MangerGetResponseOfUserEntity> getAllUsers() {
        List<User> users = userRepository.findByIsActiveTrue();
        return users.stream()
                .map(user -> MangerGetResponseOfUserEntity.builder()
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .userId(user.getUserId())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MangerGetResponseOfUserEntity> getUsers() {
        List<User> users = userRepository.findByIsActiveFalse();
        return users.stream()
                .map(user -> MangerGetResponseOfUserEntity.builder()
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .userId(user.getUserId())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ManagerDashboardOverviewResponse getManagerOverview() {
        List<Project> allProjects = projectRepository.findByIsDeletedFalse();
        List<Feature> allFeatures = featureRepository.findByIsDeletedFalse();
        List<TestCase> allTestCases = testCaseRepository.findByIsDeletedFalse();
        List<Bug> allBugs = bugRepository.findByIsDeletedFalse();
        List<User> allActiveUsers = userRepository.findByIsActiveTrue();

        // 1. Project Counts
        long totalProjects = allProjects.size();
        long activeProjects = allProjects.stream().filter(p -> p.getStatus() == ProjectStatus.ACTIVE).count();
        long inactiveProjects = totalProjects - activeProjects;

        ManagerDashboardOverviewResponse.ProjectCounts projectCounts = ManagerDashboardOverviewResponse.ProjectCounts.builder()
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .inactiveProjects(inactiveProjects)
                .build();

        // 2. Feature Counts
        long totalFeatures = allFeatures.size();
        long activeFeatures = allFeatures.stream().filter(f -> f.getStatus() == FeatureStatus.ACTIVE).count();
        long inProgressFeatures = allFeatures.stream().filter(f -> f.getStatus() == FeatureStatus.IN_PROGRESS).count();
        long completedFeatures = allFeatures.stream().filter(f -> f.getStatus() == FeatureStatus.COMPLETED).count();
        long onHoldFeatures = allFeatures.stream().filter(f -> f.getStatus() == FeatureStatus.ON_HOLD).count();
        long deprecatedFeatures = allFeatures.stream().filter(f -> f.getStatus() == FeatureStatus.DEPRECATED).count();

        ManagerDashboardOverviewResponse.FeatureCounts featureCounts = ManagerDashboardOverviewResponse.FeatureCounts.builder()
                .totalFeatures(totalFeatures)
                .activeFeatures(activeFeatures)
                .inProgressFeatures(inProgressFeatures)
                .completedFeatures(completedFeatures)
                .onHoldFeatures(onHoldFeatures)
                .deprecatedFeatures(deprecatedFeatures)
                .build();

        // 3. Test Case Metrics
        ManagerDashboardOverviewResponse.TestCaseMetrics testCaseMetrics = buildTestCaseMetrics(allTestCases);

        // 4. Bug Metrics
        ManagerDashboardOverviewResponse.BugMetrics bugMetrics = buildBugMetrics(allBugs);

        // 5. Quality Health
        QualityHealthDto qualityHealth = calculateQualityHealth(
                testCaseMetrics.getTotalTestCases(),
                testCaseMetrics.getPassedCount(),
                testCaseMetrics.getFailedCount(),
                bugMetrics.getCriticalBugs(),
                bugMetrics.getHighBugs()
        );

        // 6. Project Cards Summary
        Map<Integer, List<Feature>> featuresByProject = allFeatures.stream()
                .filter(f -> f.getProject() != null)
                .collect(Collectors.groupingBy(f -> f.getProject().getProjectId()));

        Map<Integer, List<TestCase>> testCasesByProject = allTestCases.stream()
                .filter(tc -> tc.getFeature() != null && tc.getFeature().getProject() != null)
                .collect(Collectors.groupingBy(tc -> tc.getFeature().getProject().getProjectId()));

        Map<Integer, List<Bug>> bugsByProject = allBugs.stream()
                .filter(b -> b.getFeature() != null && b.getFeature().getProject() != null)
                .collect(Collectors.groupingBy(b -> b.getFeature().getProject().getProjectId()));

        List<ManagerProjectSummaryDto> projectSummaries = allProjects.stream()
                .map(proj -> {
                    List<Feature> pFeatures = featuresByProject.getOrDefault(proj.getProjectId(), Collections.emptyList());
                    List<TestCase> pTestCases = testCasesByProject.getOrDefault(proj.getProjectId(), Collections.emptyList());
                    List<Bug> pBugs = bugsByProject.getOrDefault(proj.getProjectId(), Collections.emptyList());

                    long pTotalTc = pTestCases.size();
                    long pPassed = pTestCases.stream().filter(this::isPassed).count();
                    long pFailed = pTestCases.stream().filter(this::isFailed).count();
                    long pNoRun = pTestCases.stream().filter(this::isNoRun).count();
                    long pDescope = pTestCases.stream().filter(tc -> tc.getTestcaseStatus() == TestCaseStatus.DESCOPE).count();

                    long pTotalExecuted = pPassed + pFailed;
                    double pPassRate = pTotalExecuted > 0 ? roundOneDecimal((pPassed * 100.0) / pTotalExecuted) : 0.0;
                    double pExecRate = pTotalTc > 0 ? roundOneDecimal(((pPassed + pFailed + pDescope) * 100.0) / pTotalTc) : 0.0;

                    long pBugCount = pBugs.size();
                    long pOpenBugs = pBugs.stream().filter(this::isOpenBug).count();
                    long pCriticalBugs = pBugs.stream().filter(b -> b.getSeverity() == BugSeverity.CRITICAL && isOpenBug(b)).count();

                    QualityHealthDto pHealth = calculateQualityHealth(pTotalTc, pPassed, pFailed, pCriticalBugs,
                            pBugs.stream().filter(b -> b.getSeverity() == BugSeverity.HIGH && isOpenBug(b)).count());

                    return ManagerProjectSummaryDto.builder()
                            .projectId(proj.getProjectId())
                            .projectName(proj.getProjectName())
                            .description(proj.getDescription())
                            .region(proj.getRegion())
                            .status(proj.getStatus())
                            .createdAt(proj.getCreatedAt())
                            .updatedAt(proj.getUpdatedAt())
                            .createdByName(proj.getCreatedBy() != null ? proj.getCreatedBy().getUsername() : "N/A")
                            .featureCount((long) pFeatures.size())
                            .testCaseCount(pTotalTc)
                            .passedCount(pPassed)
                            .failedCount(pFailed)
                            .noRunCount(pNoRun)
                            .descopeCount(pDescope)
                            .passRatePercentage(pPassRate)
                            .executionRatePercentage(pExecRate)
                            .bugCount(pBugCount)
                            .openBugCount(pOpenBugs)
                            .criticalBugCount(pCriticalBugs)
                            .healthStatus(pHealth.getStatus())
                            .healthScore(pHealth.getScore())
                            .build();
                })
                .sorted(Comparator.comparing(ManagerProjectSummaryDto::getProjectId))
                .toList();

        // 7. Recent Bugs across system (top 10)
        List<ManagerRecentBugDto> recentBugs = allBugs.stream()
                .sorted(Comparator.comparing(Bug::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(this::mapToRecentBugDto)
                .toList();

        // 8. Top Failing Features across system
        Map<Integer, List<TestCase>> testCasesByFeature = allTestCases.stream()
                .filter(tc -> tc.getFeature() != null)
                .collect(Collectors.groupingBy(tc -> tc.getFeature().getFeatureId()));

        Map<Integer, List<Bug>> bugsByFeature = allBugs.stream()
                .filter(b -> b.getFeature() != null)
                .collect(Collectors.groupingBy(b -> b.getFeature().getFeatureId()));

        List<ManagerFeatureDetailMetrics> allFeatureMetrics = allFeatures.stream()
                .map(f -> buildFeatureDetailMetrics(f,
                        testCasesByFeature.getOrDefault(f.getFeatureId(), Collections.emptyList()),
                        bugsByFeature.getOrDefault(f.getFeatureId(), Collections.emptyList())))
                .toList();

        List<ManagerFeatureDetailMetrics> topFailingFeatures = allFeatureMetrics.stream()
                .filter(f -> f.getFailedCount() > 0 || f.getOpenBugs() > 0)
                .sorted(Comparator.comparing(ManagerFeatureDetailMetrics::getFailedCount, Comparator.reverseOrder())
                        .thenComparing(ManagerFeatureDetailMetrics::getCriticalBugs, Comparator.reverseOrder()))
                .limit(6)
                .toList();

        // 9. Team Metrics
        long testersCount = allActiveUsers.stream()
                .filter(u -> u.getRole() != null && "ROLE_TESTER".equalsIgnoreCase(u.getRole().getRole()))
                .count();
        long managersCount = allActiveUsers.stream()
                .filter(u -> u.getRole() != null && "ROLE_MANAGER".equalsIgnoreCase(u.getRole().getRole()))
                .count();
        long adminsCount = allActiveUsers.stream()
                .filter(u -> u.getRole() != null && "ROLE_ADMIN".equalsIgnoreCase(u.getRole().getRole()))
                .count();

        ManagerDashboardOverviewResponse.TeamMetrics teamMetrics = ManagerDashboardOverviewResponse.TeamMetrics.builder()
                .totalActiveUsers((long) allActiveUsers.size())
                .testersCount(testersCount)
                .managersCount(managersCount)
                .adminsCount(adminsCount)
                .build();

        return ManagerDashboardOverviewResponse.builder()
                .projectStats(projectCounts)
                .featureStats(featureCounts)
                .testCaseStats(testCaseMetrics)
                .bugStats(bugMetrics)
                .qualityHealth(qualityHealth)
                .projects(projectSummaries)
                .recentBugs(recentBugs)
                .topFailingFeatures(topFailingFeatures)
                .teamStats(teamMetrics)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ManagerProjectDashboardResponse getProjectDashboard(Integer projectId) {
        Project project = projectRepository.findByProjectIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        List<Feature> features = featureRepository.findByProject_ProjectIdAndIsDeletedFalse(projectId);
        List<TestCase> testCases = testCaseRepository.findByFeature_Project_ProjectIdAndIsDeletedFalse(projectId);
        List<Bug> bugs = bugRepository.findByFeature_Project_ProjectIdAndIsDeletedFalse(projectId);

        // Project Response Object
        ProjectResponse projectResponse = ProjectResponse.builder()
                .project(ProjectResponse.ProjectDetails.builder()
                        .projectId(project.getProjectId())
                        .projectName(project.getProjectName())
                        .description(project.getDescription())
                        .status(project.getStatus())
                        .region(project.getRegion())
                        .comments(project.getComments())
                        .isActive(project.isActive())
                        .isDeleted(project.isDeleted())
                        .version(project.getVersion())
                        .build())
                .audit(ProjectResponse.AuditResponse.builder()
                        .createdBy(project.getCreatedBy() != null ? project.getCreatedBy().getUserId() : null)
                        .createdByName(project.getCreatedBy() != null ? project.getCreatedBy().getUsername() : "N/A")
                        .updatedByName(project.getUpdatedBy() != null ? project.getUpdatedBy().getUsername() : null)
                        .createdAt(project.getCreatedAt())
                        .updatedAt(project.getUpdatedAt())
                        .build())
                .build();

        // Project Test Case Metrics
        ManagerDashboardOverviewResponse.TestCaseMetrics testCaseMetrics = buildTestCaseMetrics(testCases);

        // Project Bug Metrics
        ManagerDashboardOverviewResponse.BugMetrics bugMetrics = buildBugMetrics(bugs);

        // Quality Health
        QualityHealthDto qualityHealth = calculateQualityHealth(
                testCaseMetrics.getTotalTestCases(),
                testCaseMetrics.getPassedCount(),
                testCaseMetrics.getFailedCount(),
                bugMetrics.getCriticalBugs(),
                bugMetrics.getHighBugs()
        );

        // Features Breakdown
        Map<Integer, List<TestCase>> testCasesByFeature = testCases.stream()
                .filter(tc -> tc.getFeature() != null)
                .collect(Collectors.groupingBy(tc -> tc.getFeature().getFeatureId()));

        Map<Integer, List<Bug>> bugsByFeature = bugs.stream()
                .filter(b -> b.getFeature() != null)
                .collect(Collectors.groupingBy(b -> b.getFeature().getFeatureId()));

        List<ManagerFeatureDetailMetrics> featureMetricsList = features.stream()
                .map(f -> buildFeatureDetailMetrics(f,
                        testCasesByFeature.getOrDefault(f.getFeatureId(), Collections.emptyList()),
                        bugsByFeature.getOrDefault(f.getFeatureId(), Collections.emptyList())))
                .sorted(Comparator.comparing(ManagerFeatureDetailMetrics::getFeatureId))
                .toList();

        // Recent Bugs for this project (top 10)
        List<ManagerRecentBugDto> recentBugs = bugs.stream()
                .sorted(Comparator.comparing(Bug::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(this::mapToRecentBugDto)
                .toList();

        // Active contributors/testers in this project
        Set<String> contributors = new LinkedHashSet<>();
        if (project.getCreatedBy() != null) contributors.add(project.getCreatedBy().getUsername());
        for (TestCase tc : testCases) {
            if (tc.getCreatedBy() != null) contributors.add(tc.getCreatedBy().getUsername());
        }
        for (Bug b : bugs) {
            if (b.getReportedBy() != null) contributors.add(b.getReportedBy().getUsername());
            if (b.getAssignedTo() != null) contributors.add(b.getAssignedTo().getUsername());
            if (b.getExecutedBy() != null) contributors.add(b.getExecutedBy().getUsername());
        }

        return ManagerProjectDashboardResponse.builder()
                .projectDetails(projectResponse)
                .testCaseStats(testCaseMetrics)
                .bugStats(bugMetrics)
                .qualityHealth(qualityHealth)
                .features(featureMetricsList)
                .recentBugs(recentBugs)
                .activeContributors(new ArrayList<>(contributors))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ManagerFeatureDetailMetrics getFeatureDashboard(Integer featureId) {
        Feature feature = featureRepository.findByFeatureIdAndIsDeletedFalse(featureId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with ID: " + featureId));

        List<TestCase> testCases = testCaseRepository.findByFeature_FeatureIdAndIsDeletedFalse(featureId);
        List<Bug> bugs = bugRepository.findByFeature_FeatureIdAndIsDeletedFalse(featureId);

        return buildFeatureDetailMetrics(feature, testCases, bugs);
    }

    // --- Helper Methods ---

    private ManagerDashboardOverviewResponse.TestCaseMetrics buildTestCaseMetrics(List<TestCase> testCases) {
        long totalTestCases = testCases.size();
        long passedCount = testCases.stream().filter(this::isPassed).count();
        long failedCount = testCases.stream().filter(this::isFailed).count();
        long noRunCount = testCases.stream().filter(this::isNoRun).count();
        long descopeCount = testCases.stream().filter(tc -> tc.getTestcaseStatus() == TestCaseStatus.DESCOPE).count();

        long totalExecuted = passedCount + failedCount;
        double passRatePercentage = totalExecuted > 0 ? roundOneDecimal((passedCount * 100.0) / totalExecuted) : 0.0;
        double executionRatePercentage = totalTestCases > 0 ? roundOneDecimal(((passedCount + failedCount + descopeCount) * 100.0) / totalTestCases) : 0.0;

        Map<String, Long> testTypeBreakdown = testCases.stream()
                .filter(tc -> tc.getTestType() != null)
                .collect(Collectors.groupingBy(tc -> tc.getTestType().name(), Collectors.counting()));

        Map<String, Long> testPriorityBreakdown = testCases.stream()
                .filter(tc -> tc.getTestPriority() != null)
                .collect(Collectors.groupingBy(tc -> tc.getTestPriority().name(), Collectors.counting()));

        return ManagerDashboardOverviewResponse.TestCaseMetrics.builder()
                .totalTestCases(totalTestCases)
                .passedCount(passedCount)
                .failedCount(failedCount)
                .noRunCount(noRunCount)
                .descopeCount(descopeCount)
                .passRatePercentage(passRatePercentage)
                .executionRatePercentage(executionRatePercentage)
                .testTypeBreakdown(testTypeBreakdown)
                .testPriorityBreakdown(testPriorityBreakdown)
                .build();
    }

    private ManagerDashboardOverviewResponse.BugMetrics buildBugMetrics(List<Bug> bugs) {
        long totalBugs = bugs.size();
        long openBugs = bugs.stream().filter(this::isOpenBug).count();
        long inProgressBugs = bugs.stream().filter(b -> b.getStatus() == BugStatus.IN_PROGRESS).count();
        long resolvedBugs = bugs.stream().filter(b -> b.getStatus() == BugStatus.RESOLVED || b.getStatus() == BugStatus.FIXED).count();
        long closedBugs = bugs.stream().filter(b -> b.getStatus() == BugStatus.CLOSED).count();

        long criticalBugs = bugs.stream().filter(b -> b.getSeverity() == BugSeverity.CRITICAL && isOpenBug(b)).count();
        long highBugs = bugs.stream().filter(b -> b.getSeverity() == BugSeverity.HIGH && isOpenBug(b)).count();
        long mediumBugs = bugs.stream().filter(b -> b.getSeverity() == BugSeverity.MEDIUM && isOpenBug(b)).count();
        long lowBugs = bugs.stream().filter(b -> b.getSeverity() == BugSeverity.LOW && isOpenBug(b)).count();

        Map<String, Long> severityBreakdown = bugs.stream()
                .filter(b -> b.getSeverity() != null)
                .collect(Collectors.groupingBy(b -> b.getSeverity().name(), Collectors.counting()));

        Map<String, Long> priorityBreakdown = bugs.stream()
                .filter(b -> b.getPriority() != null)
                .collect(Collectors.groupingBy(b -> b.getPriority().name(), Collectors.counting()));

        Map<String, Long> categoryBreakdown = bugs.stream()
                .filter(b -> b.getCategory() != null)
                .collect(Collectors.groupingBy(b -> b.getCategory().name(), Collectors.counting()));

        return ManagerDashboardOverviewResponse.BugMetrics.builder()
                .totalBugs(totalBugs)
                .openBugs(openBugs)
                .inProgressBugs(inProgressBugs)
                .resolvedBugs(resolvedBugs)
                .closedBugs(closedBugs)
                .criticalBugs(criticalBugs)
                .highBugs(highBugs)
                .mediumBugs(mediumBugs)
                .lowBugs(lowBugs)
                .severityBreakdown(severityBreakdown)
                .priorityBreakdown(priorityBreakdown)
                .categoryBreakdown(categoryBreakdown)
                .build();
    }

    private ManagerFeatureDetailMetrics buildFeatureDetailMetrics(Feature feature, List<TestCase> testCases, List<Bug> bugs) {
        long totalTestCases = testCases.size();
        long passedCount = testCases.stream().filter(this::isPassed).count();
        long failedCount = testCases.stream().filter(this::isFailed).count();
        long noRunCount = testCases.stream().filter(this::isNoRun).count();
        long descopeCount = testCases.stream().filter(tc -> tc.getTestcaseStatus() == TestCaseStatus.DESCOPE).count();

        long totalExecuted = passedCount + failedCount;
        double passRate = totalExecuted > 0 ? roundOneDecimal((passedCount * 100.0) / totalExecuted) : 0.0;
        double execRate = totalTestCases > 0 ? roundOneDecimal(((passedCount + failedCount + descopeCount) * 100.0) / totalTestCases) : 0.0;

        long totalBugs = bugs.size();
        long openBugs = bugs.stream().filter(this::isOpenBug).count();
        long inProgressBugs = bugs.stream().filter(b -> b.getStatus() == BugStatus.IN_PROGRESS).count();
        long resolvedBugs = bugs.stream().filter(b -> b.getStatus() == BugStatus.RESOLVED || b.getStatus() == BugStatus.FIXED).count();
        long closedBugs = bugs.stream().filter(b -> b.getStatus() == BugStatus.CLOSED).count();
        long criticalBugs = bugs.stream().filter(b -> b.getSeverity() == BugSeverity.CRITICAL && isOpenBug(b)).count();
        long highBugs = bugs.stream().filter(b -> b.getSeverity() == BugSeverity.HIGH && isOpenBug(b)).count();
        long mediumBugs = bugs.stream().filter(b -> b.getSeverity() == BugSeverity.MEDIUM && isOpenBug(b)).count();
        long lowBugs = bugs.stream().filter(b -> b.getSeverity() == BugSeverity.LOW && isOpenBug(b)).count();

        QualityHealthDto health = calculateQualityHealth(totalTestCases, passedCount, failedCount, criticalBugs, highBugs);

        String healthStatus = "HEALTHY";
        if (criticalBugs > 0 || (totalExecuted > 0 && passRate < 60.0)) {
            healthStatus = "CRITICAL";
        } else if (highBugs > 0 || (totalExecuted > 0 && passRate < 85.0)) {
            healthStatus = "WARNING";
        }

        return ManagerFeatureDetailMetrics.builder()
                .featureId(feature.getFeatureId())
                .projectId(feature.getProject() != null ? feature.getProject().getProjectId() : null)
                .projectName(feature.getProject() != null ? feature.getProject().getProjectName() : "N/A")
                .featureName(feature.getFeatureName())
                .description(feature.getDescription())
                .status(feature.getStatus())
                .sprint(feature.getSprint())
                .featureVersion(feature.getFeatureVersion())
                .duration(feature.getDuration())
                .startTime(feature.getStartTime())
                .createdBy(feature.getCreatedBy() != null ? feature.getCreatedBy().getUserId() : null)
                .creatorName(feature.getCreatedBy() != null ? feature.getCreatedBy().getUsername() : "N/A")
                .createdAt(feature.getCreatedAt())
                .updatedAt(feature.getUpdatedAt())
                .totalTestCases(totalTestCases)
                .passedCount(passedCount)
                .failedCount(failedCount)
                .noRunCount(noRunCount)
                .descopeCount(descopeCount)
                .passRatePercentage(passRate)
                .executionRatePercentage(execRate)
                .totalBugs(totalBugs)
                .openBugs(openBugs)
                .inProgressBugs(inProgressBugs)
                .resolvedBugs(resolvedBugs)
                .closedBugs(closedBugs)
                .criticalBugs(criticalBugs)
                .highBugs(highBugs)
                .mediumBugs(mediumBugs)
                .lowBugs(lowBugs)
                .healthStatus(healthStatus)
                .healthScore(health.getScore())
                .build();
    }

    private ManagerRecentBugDto mapToRecentBugDto(Bug bug) {
        return ManagerRecentBugDto.builder()
                .bugId(bug.getBugId())
                .bugFormatId(bug.getBugFormatId())
                .title(bug.getTitle())
                .severity(bug.getSeverity())
                .priority(bug.getPriority())
                .status(bug.getStatus())
                .category(bug.getCategory())
                .featureId(bug.getFeature() != null ? bug.getFeature().getFeatureId() : null)
                .featureName(bug.getFeature() != null ? bug.getFeature().getFeatureName() : "N/A")
                .projectId(bug.getFeature() != null && bug.getFeature().getProject() != null ? bug.getFeature().getProject().getProjectId() : null)
                .projectName(bug.getFeature() != null && bug.getFeature().getProject() != null ? bug.getFeature().getProject().getProjectName() : "N/A")
                .reportedBy(bug.getReportedBy() != null ? bug.getReportedBy().getUsername() : "System")
                .assignedTo(bug.getAssignedTo() != null ? bug.getAssignedTo().getUsername() : "Unassigned")
                .createdAt(bug.getCreatedAt())
                .build();
    }

    private QualityHealthDto calculateQualityHealth(long totalTestCases, long passedCount, long failedCount, long criticalBugs, long highBugs) {
        long totalExecuted = passedCount + failedCount;
        double passRate = totalExecuted > 0 ? (passedCount * 100.0) / totalExecuted : (totalTestCases > 0 ? 0.0 : 100.0);

        double score = (passRate * 0.75) + 25.0;
        score -= (criticalBugs * 20.0);
        score -= (highBugs * 8.0);

        if (totalTestCases == 0) {
            score = 100.0;
        }

        score = Math.max(0.0, Math.min(100.0, score));
        score = roundOneDecimal(score);

        String status;
        String summary;

        if (criticalBugs > 0) {
            status = "CRITICAL";
            summary = criticalBugs + " Critical blocker defect(s) preventing sign-off";
        } else if (score >= 85.0 && passRate >= 85.0) {
            status = "EXCELLENT";
            summary = "Quality standards exceeded - ready for release";
        } else if (score >= 70.0) {
            status = "GOOD";
            summary = "Quality on track - minor tests or bugs in progress";
        } else if (score >= 50.0) {
            status = "NEEDS_ATTENTION";
            summary = "Test failure rate requires review before milestone";
        } else {
            status = "CRITICAL";
            summary = "Significant defect density or low pass rate detected";
        }

        return QualityHealthDto.builder()
                .score(score)
                .status(status)
                .summary(summary)
                .passRate(roundOneDecimal(passRate))
                .criticalDefectCount(criticalBugs)
                .highDefectCount(highBugs)
                .totalExecuted(totalExecuted)
                .build();
    }

    private boolean isPassed(TestCase tc) {
        if (tc == null || tc.getTestcaseStatus() == null) return false;
        return tc.getTestcaseStatus() == TestCaseStatus.PASS || tc.getTestcaseStatus() == TestCaseStatus.PASSED;
    }

    private boolean isFailed(TestCase tc) {
        if (tc == null || tc.getTestcaseStatus() == null) return false;
        return tc.getTestcaseStatus() == TestCaseStatus.FAIL || tc.getTestcaseStatus() == TestCaseStatus.FAILED;
    }

    private boolean isNoRun(TestCase tc) {
        if (tc == null || tc.getTestcaseStatus() == null) return true;
        return tc.getTestcaseStatus() == TestCaseStatus.NO_RUN;
    }

    private boolean isOpenBug(Bug b) {
        if (b == null || b.getStatus() == null) return false;
        return b.getStatus() == BugStatus.OPEN || b.getStatus() == BugStatus.REOPENED;
    }

    private double roundOneDecimal(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}
