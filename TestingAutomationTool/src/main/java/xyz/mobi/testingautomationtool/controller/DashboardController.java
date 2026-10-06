package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.DashboardDTO.*;
import xyz.mobi.testingautomationtool.service.DashboardService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/manager")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
@Tag(name = "Manager Dashboard", description = "Manager analytics, project health, feature breakdown, defect metrics & user management")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get Manager Dashboard Overview", description = "Returns high-level KPI cards, project summary list, defect trends, and failing features across all projects.")
    @GetMapping("/dashboard/overview")
    public ResponseEntity<ManagerDashboardOverviewResponse> getManagerOverview() {
        return ResponseEntity.ok(dashboardService.getManagerOverview());
    }

    @Operation(summary = "Get Project Specific Dashboard", description = "Returns professional project details, testcase pass/fail/norun/descope statistics, bug severity breakdown, and per-feature analytics.")
    @GetMapping("/dashboard/project/{projectId}")
    public ResponseEntity<ManagerProjectDashboardResponse> getProjectDashboard(@PathVariable("projectId") Integer projectId) {
        return ResponseEntity.ok(dashboardService.getProjectDashboard(projectId));
    }

    @Operation(summary = "Get Feature Specific Dashboard", description = "Returns detailed testcase pass/fail counts, execution rate, and defect metrics for a specific feature.")
    @GetMapping("/dashboard/feature/{featureId}")
    public ResponseEntity<ManagerFeatureDetailMetrics> getFeatureDashboard(@PathVariable("featureId") Integer featureId) {
        return ResponseEntity.ok(dashboardService.getFeatureDashboard(featureId));
    }

    @Operation(summary = "Confirm pending user", description = "Activates a registered user and assigns their role.")
    @PutMapping("/userConfirmation")
    public ResponseEntity<PatchResponseForManager> userConfirmation(@Valid @RequestBody PatchRequestOfManager request) {
        return ResponseEntity.ok(dashboardService.userConfirmation(request));
    }

    @Operation(summary = "Reject pending user", description = "Rejects and removes a pending user registration.")
    @DeleteMapping("/userRejection/{userId}")
    public ResponseEntity<String> userRejection(@PathVariable("userId") Integer userId) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(dashboardService.userRejection(userId));
    }

    @Operation(summary = "Get active users", description = "Returns list of all active users.")
    @GetMapping("/users/active")
    public ResponseEntity<List<MangerGetResponseOfUserEntity>> getActiveUsers() {
        return ResponseEntity.ok(dashboardService.getAllUsers());
    }

    @Operation(summary = "Get pending users", description = "Returns list of users awaiting approval.")
    @GetMapping("/users/pending")
    public ResponseEntity<List<MangerGetResponseOfUserEntity>> getPendingUsers() {
        return ResponseEntity.ok(dashboardService.getUsers());
    }
}
