package xyz.mobi.notificationservice.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import xyz.mobi.notificationservice.config.OutlookMailProperties;
import xyz.mobi.notificationservice.dto.EmailAttachmentDto;
import xyz.mobi.notificationservice.dto.EmailNotificationRequest;
import xyz.mobi.notificationservice.exception.AttachmentSizeLimitExceededException;
import xyz.mobi.notificationservice.exception.InvalidEmailRequestException;
import xyz.mobi.notificationservice.exception.UnsupportedAttachmentTypeException;

import java.net.URLConnection;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailRequestValidator {

    private final OutlookMailProperties properties;

    private static final Pattern CRLF_PATTERN = Pattern.compile("[\\r\\n]");
    private static final Pattern SAFE_HEADER_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9\\-]+$");

    private static final Set<String> DISALLOWED_HEADERS = Set.of(
            "to", "from", "cc", "bcc", "subject", "mime-version", "content-type",
            "content-transfer-encoding", "message-id", "date", "host"
    );

    public void validateRequest(EmailNotificationRequest request) {
        if (request == null) {
            throw new InvalidEmailRequestException("Request body cannot be null");
        }

        // 1. CRLF Injection check in Subject
        if (request.getSubject() != null && CRLF_PATTERN.matcher(request.getSubject()).find()) {
            throw new InvalidEmailRequestException("CRLF_INJECTION_DETECTED", "Subject line contains illegal newline characters (potential CRLF injection).");
        }

        // 2. CRLF Injection check in Reply-To
        if (request.getReplyTo() != null && CRLF_PATTERN.matcher(request.getReplyTo()).find()) {
            throw new InvalidEmailRequestException("CRLF_INJECTION_DETECTED", "Reply-To address contains illegal newline characters.");
        }

        // 4. Validate Recipient Domains if restricted
        List<String> allowedDomains = properties.getAllowedDomainsList();
        if (!allowedDomains.isEmpty()) {
            validateRecipientDomains(request.getTo(), allowedDomains, "to");
            if (request.getCc() != null) {
                validateRecipientDomains(request.getCc(), allowedDomains, "cc");
            }
            if (request.getBcc() != null) {
                validateRecipientDomains(request.getBcc(), allowedDomains, "bcc");
            }
        }

        // 5. Validate Custom Headers
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            validateHeaders(request.getHeaders());
        }

        // 6. Validate Attachments
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            validateAttachments(request.getAttachments());
        }
    }

    private void validateRecipientDomains(List<String> emails, List<String> allowedDomains, String recipientField) {
        for (String email : emails) {
            String domain = extractDomain(email);
            boolean matched = allowedDomains.stream().anyMatch(d -> domain.equalsIgnoreCase(d) || domain.endsWith("." + d));
            if (!matched) {
                throw new InvalidEmailRequestException("RECIPIENT_DOMAIN_NOT_ALLOWED",
                        "Email recipient '" + email + "' in field '" + recipientField + "' belongs to a disallowed domain. Allowed domains: " + allowedDomains);
            }
        }
    }

    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        return (atIndex >= 0 && atIndex < email.length() - 1) ? email.substring(atIndex + 1).trim() : "";
    }

    private void validateHeaders(Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();

            if (!StringUtils.hasText(name) || !SAFE_HEADER_NAME_PATTERN.matcher(name).matches()) {
                throw new InvalidEmailRequestException("INVALID_HEADER_NAME", "Header name '" + name + "' is invalid or contains prohibited characters.");
            }

            if (DISALLOWED_HEADERS.contains(name.toLowerCase())) {
                throw new InvalidEmailRequestException("PROHIBITED_HEADER", "Overriding system header '" + name + "' is not permitted.");
            }

            if (value != null && CRLF_PATTERN.matcher(value).find()) {
                throw new InvalidEmailRequestException("CRLF_INJECTION_DETECTED", "Header '" + name + "' value contains illegal newline characters (potential CRLF injection).");
            }
        }
    }

    public static class DecodedAttachment {
        private final String fileName;
        private final String contentType;
        private final byte[] data;

        public DecodedAttachment(String fileName, String contentType, byte[] data) {
            this.fileName = fileName;
            this.contentType = contentType;
            this.data = data;
        }

        public String getFileName() {
            return fileName;
        }

        public String getContentType() {
            return contentType;
        }

        public byte[] getData() {
            return data;
        }
    }

    public List<DecodedAttachment> decodeAndValidateAttachments(List<EmailAttachmentDto> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return Collections.emptyList();
        }

        validateAttachments(attachments);

        List<DecodedAttachment> decodedList = new ArrayList<>();
        long totalBytes = 0;
        List<String> blockedExtensions = properties.getBlockedExtensionsList();

        for (EmailAttachmentDto dto : attachments) {
            String fileName = sanitizeFileName(dto.getFileName());

            // Check blocked extensions
            String lowerName = fileName.toLowerCase();
            for (String blockedExt : blockedExtensions) {
                if (lowerName.endsWith(blockedExt)) {
                    throw new UnsupportedAttachmentTypeException("Attachment '" + fileName + "' has a prohibited extension (" + blockedExt + "). Executables and script files are not allowed.");
                }
            }

            // Decode safely
            byte[] bytes;
            try {
                bytes = Base64.getDecoder().decode(dto.getContentBase64().trim());
            } catch (IllegalArgumentException e) {
                throw new InvalidEmailRequestException("INVALID_BASE64_PAYLOAD", "Attachment '" + fileName + "' contains malformed Base64 content.");
            }

            if (bytes.length > properties.getMaxAttachmentSizeBytes()) {
                throw new AttachmentSizeLimitExceededException("Attachment '" + fileName + "' size (" + bytes.length + " bytes) exceeds maximum limit of " + properties.getMaxAttachmentSizeBytes() + " bytes.");
            }

            totalBytes += bytes.length;
            if (totalBytes > properties.getMaxTotalAttachmentsSizeBytes()) {
                throw new AttachmentSizeLimitExceededException("Total attachment size (" + totalBytes + " bytes) exceeds maximum allowable limit of " + properties.getMaxTotalAttachmentsSizeBytes() + " bytes.");
            }

            // Determine content type
            String contentType = dto.getContentType();
            if (!StringUtils.hasText(contentType)) {
                contentType = URLConnection.guessContentTypeFromName(fileName);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
            }

            decodedList.add(new DecodedAttachment(fileName, contentType, bytes));
        }

        return decodedList;
    }

    private void validateAttachments(List<EmailAttachmentDto> attachments) {
        if (attachments.size() > properties.getMaxAttachmentsCount()) {
            throw new InvalidEmailRequestException("ATTACHMENT_COUNT_EXCEEDED",
                    "Email contains " + attachments.size() + " attachments, exceeding maximum limit of " + properties.getMaxAttachmentsCount() + ".");
        }

        for (EmailAttachmentDto dto : attachments) {
            if (!StringUtils.hasText(dto.getFileName())) {
                throw new InvalidEmailRequestException("Attachment fileName cannot be empty");
            }
            if (dto.getFileName().contains("..") || dto.getFileName().contains("/") || dto.getFileName().contains("\\")) {
                throw new InvalidEmailRequestException("PATH_TRAVERSAL_DETECTED", "Attachment fileName '" + dto.getFileName() + "' contains invalid path traversal sequences.");
            }
            if (!StringUtils.hasText(dto.getContentBase64())) {
                throw new InvalidEmailRequestException("Attachment contentBase64 cannot be empty");
            }
        }
    }

    public String sanitizeFileName(String originalFileName) {
        if (originalFileName == null) {
            return "attachment_" + System.currentTimeMillis();
        }
        // Extract base name, strip path separators
        String clean = originalFileName.replace("\\", "/");
        int lastSlash = clean.lastIndexOf('/');
        if (lastSlash >= 0) {
            clean = clean.substring(lastSlash + 1);
        }
        return clean.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
