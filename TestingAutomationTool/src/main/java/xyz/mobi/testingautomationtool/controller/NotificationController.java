package xyz.mobi.testingautomationtool.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.NotificationResponse;
import xyz.mobi.testingautomationtool.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Get all notifications for an employee
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @PathVariable Integer employeeId) {

        return ResponseEntity.ok(
                notificationService.getMyNotifications(employeeId)
        );
    }

    // Get notification by ID
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> getById(
            @PathVariable Integer notificationId) {

        return ResponseEntity.ok(
                notificationService.getById(notificationId)
        );
    }
}