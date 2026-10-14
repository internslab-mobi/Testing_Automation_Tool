package xyz.mobi.notificationservice.service.impl;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.mobi.notificationservice.config.OutlookMailProperties;
import xyz.mobi.notificationservice.dto.EmailNotificationRequest;
import xyz.mobi.notificationservice.dto.EmailNotificationResponse;
import xyz.mobi.notificationservice.dto.NotificationHistoryDto;
import xyz.mobi.notificationservice.dto.PaginatedResponse;
import xyz.mobi.notificationservice.entity.Notification;
import xyz.mobi.notificationservice.exception.EmailDeliveryException;
import xyz.mobi.notificationservice.exception.NotificationNotFoundException;
import xyz.mobi.notificationservice.repository.NotificationRepository;
import xyz.mobi.notificationservice.service.EmailNotificationService;
import xyz.mobi.notificationservice.validator.EmailRequestValidator;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static java.time.Duration.between;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final JavaMailSender mailSender;
    private final OutlookMailProperties properties;
    private final EmailRequestValidator validator;
    private final NotificationRepository notificationRepository;

    @Override
    public EmailNotificationResponse sendEmail(EmailNotificationRequest request) {
        Instant startTime = Instant.now();

        int totalRecipients = (request.getTo() != null ? request.getTo().size() : 0)
                + (request.getCc() != null ? request.getCc().size() : 0)
                + (request.getBcc() != null ? request.getBcc().size() : 0);
        int totalAttachments = request.getAttachments() != null ? request.getAttachments().size() : 0;

        // 1. Validate request structure, headers, CRLF injection, domain policy
        validator.validateRequest(request);

        // 2. Decode and validate attachments (size limits, file extensions, Base64 validity)
        List<EmailRequestValidator.DecodedAttachment> decodedAttachments =
                validator.decodeAndValidateAttachments(request.getAttachments());

        // 3. Resolve sender address from configured Outlook mail properties
        String resolvedSender = resolveSenderAddress();

        // 4. Create and persist initial tracking record with PENDING status
        Notification notification = Notification.builder()
                .sender(resolvedSender)
                .recipients(request.getTo() != null ? String.join(", ", request.getTo()) : "")
                .ccRecipients(request.getCc() != null && !request.getCc().isEmpty() ? String.join(", ", request.getCc()) : null)
                .bccRecipients(request.getBcc() != null && !request.getBcc().isEmpty() ? String.join(", ", request.getBcc()) : null)
                .subject(request.getSubject())
                .status("PENDING")
                .createdAt(startTime)
                .build();

        notification = notificationRepository.save(notification);
        Long notificationId = notification.getId();

        log.info("Processing email dispatch: notificationId={}, recipients={}, attachments={}, isHtml={}",
                notificationId, totalRecipients, totalAttachments, request.getIsHtml());

        // 5. Construct MIME message and attempt submission via Outlook SMTP
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            boolean isMultipart = !decodedAttachments.isEmpty();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    isMultipart,
                    properties.getDefaultEncoding()
            );

            // Configure Sender (From)
            configureSender(helper, resolvedSender);

            // Configure Primary Recipients (To)
            helper.setTo(request.getTo().toArray(new String[0]));

            // Configure CC
            if (request.getCc() != null && !request.getCc().isEmpty()) {
                helper.setCc(request.getCc().toArray(new String[0]));
            }

            // Configure BCC
            if (request.getBcc() != null && !request.getBcc().isEmpty()) {
                helper.setBcc(request.getBcc().toArray(new String[0]));
            }

            // Configure Reply-To
            if (StringUtils.hasText(request.getReplyTo())) {
                helper.setReplyTo(request.getReplyTo().trim());
            }

            // Configure Subject
            helper.setSubject(request.getSubject());

            // Configure Body (HTML vs Plain Text with UTF-8 encoding)
            boolean isHtml = Boolean.TRUE.equals(request.getIsHtml());
            helper.setText(request.getBody(), isHtml);

            // Add Attachments safely
            for (EmailRequestValidator.DecodedAttachment att : decodedAttachments) {
                ByteArrayResource resource = new ByteArrayResource(att.getData());
                helper.addAttachment(att.getFileName(), resource, att.getContentType());
            }

            // Apply Validated Custom Headers (e.g. X-Priority)
            if (request.getHeaders() != null) {
                for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                    mimeMessage.setHeader(header.getKey(), header.getValue());
                }
            }

            // Always add internal tracking header
            mimeMessage.setHeader("X-Notification-ID", String.valueOf(notificationId));

            // 6. Send Message via Microsoft Outlook SMTP
            mailSender.send(mimeMessage);

            Instant submissionTime = Instant.now();
            long elapsedMillis = between(startTime, submissionTime).toMillis();
            log.info("Email submitted successfully to Outlook SMTP: notificationId={}, elapsedMs={}",
                    notificationId, elapsedMillis);

            // 7. Update status to SUBMITTED
            notification.setStatus("SUBMITTED");
            notification.setSubmittedAt(submissionTime);
            notificationRepository.save(notification);

            return EmailNotificationResponse.builder()
                    .notificationId(notificationId)
                    .status("SUBMITTED")
                    .recipientCount(totalRecipients)
                    .attachmentCount(decodedAttachments.size())
                    .submittedAt(submissionTime)
                    .build();

        } catch (Exception ex) {
            // Persist failure status and concise failure reason so failure record is not lost
            recordSubmissionFailure(notification, ex);

            if (ex instanceof MailAuthenticationException || ex instanceof AuthenticationFailedException) {
                log.error("SMTP Authentication failed for notificationId={}: {}", notificationId, ex.getMessage());
                throw new EmailDeliveryException(
                        "SMTP_AUTHENTICATION_FAILED",
                        "Failed to authenticate with Microsoft Outlook SMTP server. Verify credentials, app passwords, or Azure tenant SMTP AUTH policy."
                );
            } else if (ex instanceof MailSendException) {
                log.error("SMTP MailSendException for notificationId={}: {}", notificationId, ex.getMessage());
                throw new EmailDeliveryException(
                        "SMTP_SUBMISSION_FAILED",
                        "Microsoft Outlook SMTP rejected the message. Verify recipient addresses or mailbox send limits: " + ex.getMessage()
                );
            } else if (ex instanceof MessagingException || ex instanceof MailException) {
                log.error("MessagingException occurred for notificationId={}: {}", notificationId, ex.getMessage(), ex);
                throw new EmailDeliveryException(
                        "EMAIL_SEND_FAILED",
                        "Failed to dispatch email via Microsoft Outlook SMTP: " + ex.getMessage(),
                        ex
                );
            } else if (ex instanceof UnsupportedEncodingException) {
                log.error("Encoding error for notificationId={}: {}", notificationId, ex.getMessage());
                throw new EmailDeliveryException(
                        "ENCODING_ERROR",
                        "Character encoding error during MIME message construction: " + ex.getMessage()
                );
            } else if (ex instanceof RuntimeException runtimeException) {
                throw runtimeException;
            } else {
                throw new EmailDeliveryException("EMAIL_SEND_FAILED", "Failed to dispatch email: " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public NotificationHistoryDto getNotificationById(Long notificationId) {
        log.debug("Fetching notification tracking record for notificationId={}", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        return NotificationHistoryDto.fromEntity(notification);
    }

    @Override
    public PaginatedResponse<NotificationHistoryDto> getNotificationHistory(Pageable pageable) {
        log.debug("Fetching notification history with pageable: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<NotificationHistoryDto> springPage = notificationRepository.findAll(pageable)
                .map(NotificationHistoryDto::fromEntity);
        return PaginatedResponse.fromPage(springPage);
    }

    private void recordSubmissionFailure(Notification notification, Exception ex) {
        try {
            String conciseReason = ex.getMessage();
            if (!StringUtils.hasText(conciseReason)) {
                conciseReason = ex.getClass().getSimpleName();
            }
            if (conciseReason.length() > 990) {
                conciseReason = conciseReason.substring(0, 987) + "...";
            }
            notification.setStatus("FAILED");
            notification.setFailureReason(conciseReason);
            notificationRepository.save(notification);
        } catch (Exception dbEx) {
            log.error("Failed to record submission failure in database for notificationId={}: {}",
                    notification.getId(), dbEx.getMessage());
        }
    }

    private String resolveSenderAddress() {
        if (StringUtils.hasText(properties.getDefaultFrom())) {
            return properties.getDefaultFrom().trim();
        } else if (StringUtils.hasText(properties.getUsername())) {
            return properties.getUsername().trim();
        } else {
            return "notifications@localhost";
        }
    }

    private void configureSender(MimeMessageHelper helper, String resolvedAddress) throws MessagingException, UnsupportedEncodingException {
        String displayName = properties.getSenderDisplayName();
        if (StringUtils.hasText(displayName)) {
            helper.setFrom(new InternetAddress(resolvedAddress, displayName, properties.getDefaultEncoding()));
        } else {
            helper.setFrom(resolvedAddress);
        }
    }

}
