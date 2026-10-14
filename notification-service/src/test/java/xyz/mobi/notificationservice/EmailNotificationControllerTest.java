package xyz.mobi.notificationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import xyz.mobi.notificationservice.controller.EmailNotificationController;
import xyz.mobi.notificationservice.dto.EmailNotificationRequest;
import xyz.mobi.notificationservice.dto.EmailNotificationResponse;
import xyz.mobi.notificationservice.dto.NotificationHistoryDto;
import xyz.mobi.notificationservice.dto.PaginatedResponse;
import xyz.mobi.notificationservice.exception.EmailDeliveryException;
import xyz.mobi.notificationservice.exception.GlobalExceptionHandler;
import xyz.mobi.notificationservice.exception.NotificationNotFoundException;
import xyz.mobi.notificationservice.service.EmailNotificationService;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmailNotificationControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private EmailNotificationService notificationService;

    @InjectMocks
    private EmailNotificationController emailNotificationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(emailNotificationController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testSendEmailSuccessReturns202() throws Exception {
        EmailNotificationRequest request = EmailNotificationRequest.builder()
                .to(List.of("customer@example.com"))
                .subject("Order Confirmation #ORD-10928")
                .body("<p>Thank you for your order.</p>")
                .isHtml(true)
                .build();

        Long notificationId = 1001L;
        EmailNotificationResponse mockResponse = EmailNotificationResponse.builder()
                .notificationId(notificationId)
                .status("SUBMITTED")
                .recipientCount(1)
                .attachmentCount(0)
                .submittedAt(Instant.now())
                .build();

        when(notificationService.sendEmail(any(EmailNotificationRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email accepted for sending"))
                .andExpect(jsonPath("$.data.notificationId").value(notificationId))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }

    @Test
    void testSendEmailWithMissingFieldsReturns400() throws Exception {
        EmailNotificationRequest invalidRequest = EmailNotificationRequest.builder()
                .to(List.of()) // Empty recipients list
                .subject("")   // Blank subject
                .body("")      // Blank body
                .build();

        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors.to").exists())
                .andExpect(jsonPath("$.validationErrors.subject").exists())
                .andExpect(jsonPath("$.validationErrors.body").exists());
    }

    @Test
    void testSendEmailDeliveryFailureReturns502() throws Exception {
        EmailNotificationRequest request = EmailNotificationRequest.builder()
                .to(List.of("customer@example.com"))
                .subject("Notification Subject")
                .body("Body content")
                .build();

        when(notificationService.sendEmail(any(EmailNotificationRequest.class)))
                .thenThrow(new EmailDeliveryException("SMTP_AUTHENTICATION_FAILED", "Failed to authenticate with Microsoft Outlook SMTP server."));

        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("SMTP_AUTHENTICATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Failed to authenticate with Microsoft Outlook SMTP server."));
    }

    @Test
    void testGetNotificationByIdSuccessReturns200() throws Exception {
        Long notificationId = 998877L;
        NotificationHistoryDto historyDto = NotificationHistoryDto.builder()
                .notificationId(notificationId)
                .sender("sender@company.com")
                .to(List.of("recipient@example.com"))
                .subject("Order Confirmation #ORD-10928")
                .status("SUBMITTED")
                .createdAt(Instant.now())
                .submittedAt(Instant.now())
                .build();

        when(notificationService.getNotificationById(notificationId)).thenReturn(historyDto);

        mockMvc.perform(get("/api/v1/notifications/{notificationId}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notificationId").value(notificationId))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.to[0]").value("recipient@example.com"));
    }

    @Test
    void testGetNotificationByIdNotFoundReturns404() throws Exception {
        Long unknownId = 999999L;
        when(notificationService.getNotificationById(unknownId))
                .thenThrow(new NotificationNotFoundException(unknownId));

        mockMvc.perform(get("/api/v1/notifications/{notificationId}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("NOTIFICATION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Notification record with ID '999999' not found."));
    }

    @Test
    void testGetNotificationHistoryReturns200() throws Exception {
        NotificationHistoryDto historyDto = NotificationHistoryDto.builder()
                .notificationId(1L)
                .sender("sender@company.com")
                .to(List.of("user@example.com"))
                .subject("Billing Invoice")
                .status("SUBMITTED")
                .createdAt(Instant.now())
                .build();

        when(notificationService.getNotificationHistory(any(Pageable.class)))
                .thenReturn(PaginatedResponse.<NotificationHistoryDto>builder()
                        .items(List.of(historyDto))
                        .page(0)
                        .size(10)
                        .totalElements(1)
                        .totalPages(1)
                        .last(true)
                        .build());

        mockMvc.perform(get("/api/v1/notifications?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].notificationId").value(1))
                .andExpect(jsonPath("$.data.items[0].status").value("SUBMITTED"));
    }
}
