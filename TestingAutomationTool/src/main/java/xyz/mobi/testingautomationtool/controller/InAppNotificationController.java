package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.NotificationDto.InAppNotificationResponse;
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
    public ResponseEntity<List<InAppNotificationResponse>> getMyNotifications() {
        return ResponseEntity.ok(inAppNotificationService.getMyNotifications());
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer notificationId) {
        inAppNotificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Integer notificationId) {
        inAppNotificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
