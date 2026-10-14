package xyz.mobi.notificationservice;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import xyz.mobi.notificationservice.config.OutlookMailProperties;
import xyz.mobi.notificationservice.dto.EmailAttachmentDto;
import xyz.mobi.notificationservice.dto.EmailNotificationRequest;
import xyz.mobi.notificationservice.dto.EmailNotificationResponse;
import xyz.mobi.notificationservice.dto.NotificationHistoryDto;
import xyz.mobi.notificationservice.dto.PaginatedResponse;
import xyz.mobi.notificationservice.entity.Notification;
import xyz.mobi.notificationservice.exception.EmailDeliveryException;
import xyz.mobi.notificationservice.exception.NotificationNotFoundException;
import xyz.mobi.notificationservice.repository.NotificationRepository;
import xyz.mobi.notificationservice.service.impl.EmailNotificationServiceImpl;
import xyz.mobi.notificationservice.validator.EmailRequestValidator;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutlookEmailNotificationServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationRepository notificationRepository;

    private OutlookMailProperties properties;
    private EmailRequestValidator validator;
    private EmailNotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new OutlookMailProperties();
        properties.setUsername("notifications@company.com");
        properties.setDefaultFrom("notifications@company.com");
        properties.setSenderDisplayName("Notification Service");
        properties.setDefaultEncoding("UTF-8");

        validator = new EmailRequestValidator(properties);
        service = new EmailNotificationServiceImpl(mailSender, properties, validator, notificationRepository);

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        lenient().when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void testSendHtmlEmailSuccessfullyPersistsAndSubmits() {
        EmailNotificationRequest request = EmailNotificationRequest.builder()
                .to(List.of("customer@example.com"))
                .cc(List.of("support@example.com"))
                .subject("Order Confirmation #ORD-10928")
                .body("<h1>Order Confirmed</h1><p>Thank you for your order.</p>")
                .isHtml(true)
                .build();

        doNothing().when(mailSender).send(any(MimeMessage.class));

        EmailNotificationResponse response = service.sendEmail(request);

        assertNotNull(response);
        assertNotNull(response.getNotificationId());
        assertEquals("SUBMITTED", response.getStatus());
        assertEquals(2, response.getRecipientCount());
        assertEquals(0, response.getAttachmentCount());

        verify(mailSender, times(1)).send(any(MimeMessage.class));

        // Verify entity was saved: initial PENDING then updated to SUBMITTED
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, atLeast(2)).save(captor.capture());
        Notification lastSaved = captor.getValue();
        assertEquals("SUBMITTED", lastSaved.getStatus());
        assertEquals("notifications@company.com", lastSaved.getSender());
        assertEquals("Order Confirmation #ORD-10928", lastSaved.getSubject());
        assertEquals("customer@example.com", lastSaved.getRecipients());
        assertEquals("support@example.com", lastSaved.getCcRecipients());
        assertNotNull(lastSaved.getSubmittedAt());
    }

    @Test
    void testSendPlainTextEmailWithAttachmentsSuccessfully() {
        String testContent = "Invoice data line 1\nline 2";
        String base64Content = Base64.getEncoder().encodeToString(testContent.getBytes(StandardCharsets.UTF_8));

        EmailAttachmentDto attachment = EmailAttachmentDto.builder()
                .fileName("invoice_1024.pdf")
                .contentType("application/pdf")
                .contentBase64(base64Content)
                .build();

        EmailNotificationRequest request = EmailNotificationRequest.builder()
                .to(List.of("billing@example.com"))
                .subject("Invoice Attached")
                .body("Please find attached the invoice.")
                .isHtml(false)
                .attachments(List.of(attachment))
                .build();

        doNothing().when(mailSender).send(any(MimeMessage.class));

        EmailNotificationResponse response = service.sendEmail(request);

        assertNotNull(response);
        assertEquals("SUBMITTED", response.getStatus());
        assertEquals(1, response.getRecipientCount());
        assertEquals(1, response.getAttachmentCount());

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSmtpAuthenticationFailureMapsToEmailDeliveryExceptionAndPersistsFailure() {
        EmailNotificationRequest request = EmailNotificationRequest.builder()
                .to(List.of("engineer@example.com"))
                .subject("System Alert")
                .body("Alert body content")
                .build();

        doThrow(new MailAuthenticationException("535 5.7.139 Authentication unsuccessful"))
                .when(mailSender).send(any(MimeMessage.class));

        EmailDeliveryException ex = assertThrows(EmailDeliveryException.class,
                () -> service.sendEmail(request));

        assertEquals("SMTP_AUTHENTICATION_FAILED", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Failed to authenticate with Microsoft Outlook SMTP server"));

        // Verify failure was persisted with status FAILED and reason
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, atLeast(2)).save(captor.capture());
        Notification lastSaved = captor.getValue();
        assertEquals("FAILED", lastSaved.getStatus());
        assertNotNull(lastSaved.getFailureReason());
        assertTrue(lastSaved.getFailureReason().contains("535 5.7.139 Authentication unsuccessful"));
    }

    @Test
    void testSmtpMailSendExceptionMapsToEmailDeliveryExceptionAndPersistsFailure() {
        EmailNotificationRequest request = EmailNotificationRequest.builder()
                .to(List.of("engineer@example.com"))
                .subject("System Alert")
                .body("Alert body content")
                .build();

        doThrow(new MailSendException("550 5.1.1 User unknown"))
                .when(mailSender).send(any(MimeMessage.class));

        EmailDeliveryException ex = assertThrows(EmailDeliveryException.class,
                () -> service.sendEmail(request));

        assertEquals("SMTP_SUBMISSION_FAILED", ex.getErrorCode());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, atLeast(2)).save(captor.capture());
        Notification lastSaved = captor.getValue();
        assertEquals("FAILED", lastSaved.getStatus());
        assertTrue(lastSaved.getFailureReason().contains("550 5.1.1 User unknown"));
    }

    @Test
    void testGetNotificationByIdFound() {
        String id = "notif-12345";
        Notification notification = Notification.builder()
                .id(id)
                .sender("sender@example.com")
                .recipients("user1@example.com, user2@example.com")
                .ccRecipients("manager@example.com")
                .subject("Weekly Update")
                .status("SUBMITTED")
                .createdAt(Instant.now())
                .submittedAt(Instant.now())
                .build();

        when(notificationRepository.findById(id)).thenReturn(Optional.of(notification));

        NotificationHistoryDto history = service.getNotificationById(id);

        assertNotNull(history);
        assertEquals(id, history.getNotificationId());
        assertEquals("sender@example.com", history.getSender());
        assertEquals(List.of("user1@example.com", "user2@example.com"), history.getTo());
        assertEquals(List.of("manager@example.com"), history.getCc());
        assertEquals("SUBMITTED", history.getStatus());
    }

    @Test
    void testGetNotificationByIdNotFoundThrowsException() {
        when(notificationRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class,
                () -> service.getNotificationById("unknown-id"));
    }

    @Test
    void testGetNotificationHistoryPaginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Notification notif = Notification.builder()
                .id("notif-1")
                .sender("sender@example.com")
                .recipients("user@example.com")
                .subject("Notice")
                .status("SUBMITTED")
                .createdAt(Instant.now())
                .build();

        when(notificationRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(notif)));

        PaginatedResponse<NotificationHistoryDto> page = service.getNotificationHistory(pageable);

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        assertEquals("notif-1", page.getItems().get(0).getNotificationId());
    }
}
