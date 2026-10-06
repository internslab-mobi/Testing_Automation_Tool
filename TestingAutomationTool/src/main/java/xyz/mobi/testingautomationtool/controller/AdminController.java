package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import xyz.mobi.testingautomationtool.dto.AdminDTO.CreateManagerRequest;
import xyz.mobi.testingautomationtool.dto.AdminDTO.ManagerResponse;
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
    public ResponseEntity<ManagerResponse> createManager(
            @Valid @RequestBody CreateManagerRequest request) {

        ManagerResponse response = adminService.createManager(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/managers/{userId}")
    public ResponseEntity<ManagerResponse> updateManager(
            @PathVariable Integer userId,
            @Valid @RequestBody CreateManagerRequest request) {

        ManagerResponse response =
                adminService.updateManager(userId, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/managers/{userId}")
    public ResponseEntity<String> patchManager(
            @PathVariable Integer userId,
            @RequestBody CreateManagerRequest request) {

        String response = adminService.patchManager(userId, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ManagerResponse> getUserById(
            @PathVariable Integer userId) {

        ManagerResponse response =
                adminService.getUserById(userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<ManagerResponse>> getAllUsers() {

        List<ManagerResponse> response =
                adminService.getAllUsers();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/users/{role}")
    public ResponseEntity<List<ManagerResponse>> getUsersByRole(
            @RequestParam UserRole role
            ){

        List<ManagerResponse> response =
                adminService.getUsersByRole(role);

        return ResponseEntity.ok(response);
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