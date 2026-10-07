package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.AdminDTO.CreateManagerRequest;
import xyz.mobi.testingautomationtool.dto.AdminDTO.ManagerResponse;
import xyz.mobi.testingautomationtool.dto.ApiResponse;
import xyz.mobi.testingautomationtool.enums.UserRole;
import xyz.mobi.testingautomationtool.service.AdminService;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/managers")
    public ResponseEntity<ApiResponse<ManagerResponse>> createManager(
            @Valid @RequestBody CreateManagerRequest request) {

        ManagerResponse response = adminService.createManager(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Manager created successfully", response));
    }

    @PutMapping("/managers/{userId}")
    public ResponseEntity<ApiResponse<ManagerResponse>> updateManager(
            @PathVariable Integer userId,
            @Valid @RequestBody CreateManagerRequest request) {

        ManagerResponse response =
                adminService.updateManager(userId, request);

        return ResponseEntity.ok(ApiResponse.success("Manager updated successfully", response));
    }

    @PatchMapping("/managers/{userId}")
    public ResponseEntity<ApiResponse<String>> patchManager(
            @PathVariable Integer userId,
            @RequestBody CreateManagerRequest request) {

        String response = adminService.patchManager(userId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<ManagerResponse>> getUserById(
            @PathVariable Integer userId) {

        ManagerResponse response =
                adminService.getUserById(userId);

        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<ManagerResponse>>> getAllUsers() {

        List<ManagerResponse> response =
                adminService.getAllUsers();

        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }

    @GetMapping("/users/{role}")
    public ResponseEntity<ApiResponse<List<ManagerResponse>>> getUsersByRole(
            @RequestParam UserRole role) {

        List<ManagerResponse> response =
                adminService.getUsersByRole(role);

        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<String>> softDeleteUser(
            @PathVariable Integer userId) {

        adminService.softDeleteUser(userId);

        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @DeleteMapping("/users/{userId}/permanent")
    public ResponseEntity<ApiResponse<String>> hardDeleteUser(
            @PathVariable Integer userId) {

        adminService.hardDeleteUser(userId);

        return ResponseEntity.ok(ApiResponse.success("User permanently deleted successfully"));
    }

    @DeleteMapping("/projects/{projectId}/permanent")
    public ResponseEntity<ApiResponse<String>> hardDeleteProject(
            @PathVariable Integer projectId) {

        adminService.hardDeleteProject(projectId);

        return ResponseEntity.ok(ApiResponse.success("Project permanently deleted successfully"));
    }

    @DeleteMapping("/features/{featureId}/permanent")
    public ResponseEntity<ApiResponse<String>> hardDeleteFeature(
            @PathVariable Integer featureId) {

        adminService.hardDeleteFeature(featureId);

        return ResponseEntity.ok(ApiResponse.success("Feature permanently deleted successfully"));
    }

    @DeleteMapping("/testcases/{testCaseId}/permanent")
    public ResponseEntity<ApiResponse<String>> hardDeleteTestCase(
            @PathVariable Integer testCaseId) {

        adminService.hardDeleteTestCase(testCaseId);

        return ResponseEntity.ok(ApiResponse.success("Test case permanently deleted successfully"));
    }

    @DeleteMapping("/bugs/{bugId}/permanent")
    public ResponseEntity<ApiResponse<String>> hardDeleteBug(
            @PathVariable Integer bugId) {

        adminService.hardDeleteBug(bugId);

        return ResponseEntity.ok(ApiResponse.success("Bug permanently deleted successfully"));
    }
}