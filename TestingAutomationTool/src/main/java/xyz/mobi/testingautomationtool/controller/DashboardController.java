package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.DashboardDto.MangerGetResponseOfUserEntity;
import xyz.mobi.testingautomationtool.dto.DashboardDto.PatchRequestOfManager;
import xyz.mobi.testingautomationtool.dto.DashboardDto.PatchResponseForManager;
import xyz.mobi.testingautomationtool.service.DashboardService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/manager")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('MANAGER')")
public class DashboardController {

    private final DashboardService dashboardService;

    @PutMapping("/userConfirmation")
    public ResponseEntity<PatchResponseForManager> userConfirmation(@Valid @RequestBody PatchRequestOfManager request) {
        return ResponseEntity.ok(dashboardService.userConfirmation(request));
    }

    @DeleteMapping("/userRejection/{userId}")
    public ResponseEntity<String> userRejection(@PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(dashboardService.userRejection(userId));
    }

    @GetMapping("/users/active")
    public ResponseEntity<List<MangerGetResponseOfUserEntity>> getActiveUsers() {
        return ResponseEntity.ok(dashboardService.getAllUsers());
    }

    @GetMapping("/users/pending")
    public ResponseEntity<List<MangerGetResponseOfUserEntity>> getPendingUsers() {
        return ResponseEntity.ok(dashboardService.getUsers());
    }
}
