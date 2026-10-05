package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import xyz.mobi.testingautomationtool.dto.AdminDto.CreateManagerRequest;
import xyz.mobi.testingautomationtool.service.AdminService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/managers")
    public ResponseEntity<String> createManager(
            @Valid @RequestBody CreateManagerRequest request) {

        adminService.createManager(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Manager created successfully");
    }


    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> softDeleteUser(
            @PathVariable Integer userId) {

        adminService.softDeleteUser(userId);

        return ResponseEntity.ok(
                "User deleted successfully"
        );
    }


    @DeleteMapping("/users/{userId}/permanent")
    public ResponseEntity<String> hardDeleteUser(
            @PathVariable Integer userId) {

        adminService.hardDeleteUser(userId);

        return ResponseEntity.ok(
                "User permanently deleted successfully"
        );
    }


    @DeleteMapping("/projects/{projectId}/permanent")
    public ResponseEntity<String> hardDeleteProject(
            @PathVariable Integer projectId) {

        adminService.hardDeleteProject(projectId);

        return ResponseEntity.ok(
                "Project permanently deleted successfully"
        );
    }


    @DeleteMapping("/features/{featureId}/permanent")
    public ResponseEntity<String> hardDeleteFeature(
            @PathVariable Integer featureId) {

        adminService.hardDeleteFeature(featureId);

        return ResponseEntity.ok(
                "Feature permanently deleted successfully"
        );
    }


    @DeleteMapping("/testcases/{testCaseId}/permanent")
    public ResponseEntity<String> hardDeleteTestCase(
            @PathVariable Integer testCaseId) {

        adminService.hardDeleteTestCase(testCaseId);

        return ResponseEntity.ok(
                "Test case permanently deleted successfully"
        );
    }


    @DeleteMapping("/bugs/{bugId}/permanent")
    public ResponseEntity<String> hardDeleteBug(
            @PathVariable Integer bugId) {

        adminService.hardDeleteBug(bugId);

        return ResponseEntity.ok(
                "Bug permanently deleted successfully"
        );
    }
}