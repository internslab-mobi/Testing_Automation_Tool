package xyz.mobi.notificationservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.mobi.notificationservice.config.OutlookMailProperties;
import xyz.mobi.notificationservice.dto.EmailAttachmentDto;
import xyz.mobi.notificationservice.dto.EmailNotificationRequest;
import xyz.mobi.notificationservice.exception.AttachmentSizeLimitExceededException;
import xyz.mobi.notificationservice.exception.InvalidEmailRequestException;
import xyz.mobi.notificationservice.exception.UnsupportedAttachmentTypeException;
import xyz.mobi.notificationservice.validator.EmailRequestValidator;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmailRequestValidatorTest {

    private OutlookMailProperties properties;
    private EmailRequestValidator validator;

    @BeforeEach
    void setUp() {
        properties = new OutlookMailProperties();
        properties.setMaxAttachmentSizeBytes(1024 * 1024); // 1 MB
        properties.setMaxTotalAttachmentsSizeBytes(2 * 1024 * 1024); // 2 MB
        properties.setMaxAttachmentsCount(5);
        properties.setBlockedFileExtensions(".exe,.bat,.sh,.cmd");
        validator = new EmailRequestValidator(properties);
    }

    @Test
    void testValidRequestPasses() {
        EmailNotificationRequest request = EmailNotificationRequest.builder()
                .to(List.of("qa@example.com"))
                .subject("Test Execution Passed")
                .body("All tests passed.")
                .isHtml(false)
                .build();

        assertDoesNotThrow(() -> validator.validateRequest(request));
    }

    @Test
    void testSubjectWithCRLFThrowsException() {
        EmailNotificationRequest request = EmailNotificationRequest.builder()
                .to(List.of("qa@example.com"))
                .subject("Test Execution\r\nBcc: hacker@example.com")
                .body("All tests passed.")
                .build();

        InvalidEmailRequestException ex = assertThrows(InvalidEmailRequestException.class,
                () -> validator.validateRequest(request));
        assertEquals("CRLF_INJECTION_DETECTED", ex.getErrorCode());
    }

    @Test
    void testProhibitedHeaderThrowsException() {
        EmailNotificationRequest request = EmailNotificationRequest.builder()
                .to(List.of("qa@example.com"))
                .subject("Report")
                .body("Report body")
                .headers(Map.of("Bcc", "hidden@example.com"))
                .build();

        InvalidEmailRequestException ex = assertThrows(InvalidEmailRequestException.class,
                () -> validator.validateRequest(request));
        assertEquals("PROHIBITED_HEADER", ex.getErrorCode());
    }

    @Test
    void testHeaderWithCRLFThrowsException() {
        EmailNotificationRequest request = EmailNotificationRequest.builder()
                .to(List.of("qa@example.com"))
                .subject("Report")
                .body("Report body")
                .headers(Map.of("X-Custom-Tag", "TagValue\r\nInjected: true"))
                .build();

        InvalidEmailRequestException ex = assertThrows(InvalidEmailRequestException.class,
                () -> validator.validateRequest(request));
        assertEquals("CRLF_INJECTION_DETECTED", ex.getErrorCode());
    }

    @Test
    void testPathTraversalInAttachmentFilenameThrowsException() {
        EmailAttachmentDto attachment = EmailAttachmentDto.builder()
                .fileName("../../etc/passwd")
                .contentBase64(Base64.getEncoder().encodeToString("content".getBytes(StandardCharsets.UTF_8)))
                .build();

        List<EmailAttachmentDto> list = List.of(attachment);
        InvalidEmailRequestException ex = assertThrows(InvalidEmailRequestException.class,
                () -> validator.decodeAndValidateAttachments(list));
        assertEquals("PATH_TRAVERSAL_DETECTED", ex.getErrorCode());
    }

    @Test
    void testExecutableAttachmentThrowsUnsupportedAttachmentTypeException() {
        EmailAttachmentDto attachment = EmailAttachmentDto.builder()
                .fileName("malicious.exe")
                .contentBase64(Base64.getEncoder().encodeToString("payload".getBytes(StandardCharsets.UTF_8)))
                .build();

        assertThrows(UnsupportedAttachmentTypeException.class,
                () -> validator.decodeAndValidateAttachments(List.of(attachment)));
    }

    @Test
    void testMalformedBase64ThrowsInvalidEmailRequestException() {
        EmailAttachmentDto attachment = EmailAttachmentDto.builder()
                .fileName("report.txt")
                .contentBase64("Not Valid Base64 !!! @@##")
                .build();

        InvalidEmailRequestException ex = assertThrows(InvalidEmailRequestException.class,
                () -> validator.decodeAndValidateAttachments(List.of(attachment)));
        assertEquals("INVALID_BASE64_PAYLOAD", ex.getErrorCode());
    }

    @Test
    void testAttachmentExceedingSizeThrowsException() {
        byte[] largeBytes = new byte[1024 * 1024 + 10]; // Exceeds 1 MB
        EmailAttachmentDto attachment = EmailAttachmentDto.builder()
                .fileName("large_data.bin")
                .contentBase64(Base64.getEncoder().encodeToString(largeBytes))
                .build();

        assertThrows(AttachmentSizeLimitExceededException.class,
                () -> validator.decodeAndValidateAttachments(List.of(attachment)));
    }

    @Test
    void testAllowedRecipientDomainsValidation() {
        properties.setAllowedRecipientDomains("mobi.xyz,company.org");

        EmailNotificationRequest validRequest = EmailNotificationRequest.builder()
                .to(List.of("lead@mobi.xyz", "manager@company.org"))
                .subject("Allowed domain")
                .body("Hello")
                .build();
        assertDoesNotThrow(() -> validator.validateRequest(validRequest));

        EmailNotificationRequest invalidRequest = EmailNotificationRequest.builder()
                .to(List.of("external@gmail.com"))
                .subject("Blocked domain")
                .body("Hello")
                .build();
        InvalidEmailRequestException ex = assertThrows(InvalidEmailRequestException.class,
                () -> validator.validateRequest(invalidRequest));
        assertEquals("RECIPIENT_DOMAIN_NOT_ALLOWED", ex.getErrorCode());
    }
}
