package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import xyz.mobi.testingautomationtool.dto.AdminDTO.CreateManagerRequest;
import xyz.mobi.testingautomationtool.dto.AdminDTO.ManagerResponse;
import xyz.mobi.testingautomationtool.service.AdminService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/managers")
    public ResponseEntity<ManagerResponse> createManager(
            @Valid @RequestBody CreateManagerRequest request) {

        ManagerResponse response = adminService.createManager(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> softDeleteUser(
            @PathVariable Integer userId) {

        adminService.softDeleteUser(userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                "User deleted successfully"
        );
    }


    @DeleteMapping("/users/{userId}/permanent")
    public ResponseEntity<String> hardDeleteUser(
            @PathVariable Integer userId) {

        adminService.hardDeleteUser(userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                "User permanently deleted successfully"
        );
    }


    @DeleteMapping("/projects/{projectId}/permanent")
    public ResponseEntity<String> hardDeleteProject(
            @PathVariable Integer projectId) {

        adminService.hardDeleteProject(projectId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                "Project permanently deleted successfully"
        );
    }


    @DeleteMapping("/features/{featureId}/permanent")
    public ResponseEntity<String> hardDeleteFeature(
            @PathVariable Integer featureId) {

        adminService.hardDeleteFeature(featureId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                "Feature permanently deleted successfully"
        );
    }


    @DeleteMapping("/testcases/{testCaseId}/permanent")
    public ResponseEntity<String> hardDeleteTestCase(
            @PathVariable Integer testCaseId) {

        adminService.hardDeleteTestCase(testCaseId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                "Test case permanently deleted successfully"
        );
    }


    @DeleteMapping("/bugs/{bugId}/permanent")
    public ResponseEntity<String> hardDeleteBug(
            @PathVariable Integer bugId) {

        adminService.hardDeleteBug(bugId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                "Bug permanently deleted successfully"
        );
    }
}