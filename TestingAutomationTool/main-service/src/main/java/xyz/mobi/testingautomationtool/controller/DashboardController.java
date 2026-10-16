package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.ApiResponse;
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

    @Operation(summary = "Confirm pending user", description = "Activates a registered user and assigns their role.")
    @PutMapping("/userConfirmation")
    public ResponseEntity<ApiResponse<PatchResponseForManager>> userConfirmation(@Valid @RequestBody PatchRequestOfManager request) {
        return ResponseEntity.ok(ApiResponse.success("User confirmed successfully", dashboardService.userConfirmation(request)));
    }

    @Operation(summary = "Reject pending user", description = "Rejects and removes a pending user registration.")
    @DeleteMapping("/userRejection/{userId}")
    public ResponseEntity<ApiResponse<String>> userRejection(@PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.userRejection(userId)));
    }

    @Operation(summary = "Get active users", description = "Returns list of all active users.")
    @GetMapping("/users/active")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'TESTER')")
    public ResponseEntity<ApiResponse<List<MangerGetResponseOfUserEntity>>> getActiveUsers() {
        return ResponseEntity.ok(ApiResponse.success("Active users retrieved successfully", dashboardService.getAllUsers()));
    }

    @Operation(summary = "Get pending users", description = "Returns list of users awaiting approval.")
    @GetMapping("/users/pending")
    public ResponseEntity<ApiResponse<List<MangerGetResponseOfUserEntity>>> getPendingUsers() {
        return ResponseEntity.ok(ApiResponse.success("Pending users retrieved successfully", dashboardService.getUsers()));
    }
}
