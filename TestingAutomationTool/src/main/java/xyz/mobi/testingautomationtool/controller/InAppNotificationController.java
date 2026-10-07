package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.ApiResponse;
import xyz.mobi.testingautomationtool.dto.NotificationDTO.*;
import xyz.mobi.testingautomationtool.service.InAppNotificationService;

import java.util.List;

@RestController
@RequestMapping("/in-app-notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
public class InAppNotificationController {

    private final InAppNotificationService inAppNotificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InAppNotificationResponse>>> getMyNotifications() {
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", inAppNotificationService.getMyNotifications()));
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<String>> patchNotification(
            @PathVariable Integer notificationId,
            @RequestBody(required = false) NotificationPatchRequest request) {
        String response = inAppNotificationService.patchNotification(notificationId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable Integer notificationId) {
        inAppNotificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully"));
    }
}
