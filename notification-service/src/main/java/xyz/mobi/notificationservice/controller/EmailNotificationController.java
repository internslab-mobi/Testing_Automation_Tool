package xyz.mobi.notificationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.notificationservice.dto.ApiResponse;
import xyz.mobi.notificationservice.dto.EmailNotificationRequest;
import xyz.mobi.notificationservice.dto.EmailNotificationResponse;
import xyz.mobi.notificationservice.dto.NotificationHistoryDto;
import xyz.mobi.notificationservice.dto.PaginatedResponse;
import xyz.mobi.notificationservice.service.EmailNotificationService;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class EmailNotificationController {

    private final EmailNotificationService notificationService;

    @PostMapping("/email")
    public ResponseEntity<ApiResponse<EmailNotificationResponse>> sendEmail(
            @Valid @RequestBody EmailNotificationRequest request) {

        log.info("Received request to send email: subject='{}'", request.getSubject());
        EmailNotificationResponse response = notificationService.sendEmail(request);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Email accepted for sending", response));
    }


    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationHistoryDto>> getNotificationById(
            @PathVariable Long notificationId) {

        log.info("Received request for notification history: notificationId={}", notificationId);
        NotificationHistoryDto history = notificationService.getNotificationById(notificationId);

        return ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully", history));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<NotificationHistoryDto>>> getNotificationHistory(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Received request for paginated notification history: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        PaginatedResponse<NotificationHistoryDto> historyPage = notificationService.getNotificationHistory(pageable);

        return ResponseEntity.ok(ApiResponse.success("Notification history retrieved successfully", historyPage));
    }
}

