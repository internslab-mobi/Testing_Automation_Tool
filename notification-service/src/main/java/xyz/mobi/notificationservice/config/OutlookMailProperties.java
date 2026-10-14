package xyz.mobi.notificationservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "notification.mail.outlook")
public class OutlookMailProperties {

    private String host = "smtp.office365.com";
    private int port = 587;
    private String username;
    private String password;
    private boolean auth = true;
    private StartTlsProperties starttls = new StartTlsProperties();
    private String defaultFrom;
    private String senderDisplayName = "Notification Service";
    private String defaultEncoding = "UTF-8";

    private int connectionTimeout = 10000;
    private int readTimeout = 15000;
    private int writeTimeout = 15000;

    private long maxAttachmentSizeBytes = 10 * 1024 * 1024; // 10 MB
    private long maxTotalAttachmentsSizeBytes = 25 * 1024 * 1024; // 25 MB
    private int maxAttachmentsCount = 10;

    private String blockedFileExtensions = ".exe,.bat,.cmd,.sh,.vbs,.js,.ps1,.scr,.pif,.jar";
    private String allowedRecipientDomains = "";

    @Data
    public static class StartTlsProperties {
        private boolean enable = true;
        private boolean required = true;
    }

    public List<String> getBlockedExtensionsList() {
        if (blockedFileExtensions == null || blockedFileExtensions.isBlank()) {
            return List.of();
        }
        String[] parts = blockedFileExtensions.split(",");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                list.add(trimmed.startsWith(".") ? trimmed : "." + trimmed);
            }
        }
        return list;
    }

    public List<String> getAllowedDomainsList() {
        if (allowedRecipientDomains == null || allowedRecipientDomains.isBlank()) {
            return List.of();
        }
        String[] parts = allowedRecipientDomains.split(",");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }
}
