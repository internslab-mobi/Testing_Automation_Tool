package xyz.mobi.testingautomationtool.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.response.getMethodDTO.InAppNotificationResponse;
import xyz.mobi.testingautomationtool.service.InAppNotificationService;

import java.util.List;

@RestController
@RequestMapping("/in-app-notifications")
@RequiredArgsConstructor
public class InAppNotificationController {

    private final InAppNotificationService inAppNotificationService;

    @GetMapping
    public ResponseEntity<List<InAppNotificationResponse>> getMyNotifications() {

        return ResponseEntity.ok(inAppNotificationService.getMyNotifications());
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Integer notificationId) {

        inAppNotificationService.markAsRead(notificationId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Integer notificationId) {

        inAppNotificationService.deleteNotification(notificationId);

        return ResponseEntity.noContent().build();
    }
}
