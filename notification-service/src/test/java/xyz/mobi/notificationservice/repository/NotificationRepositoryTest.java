package xyz.mobi.notificationservice.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import xyz.mobi.notificationservice.entity.Notification;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private Notification notif1;
    private Notification notif2;
    private Notification notif3;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();

        Instant now = Instant.now();

        notif1 = Notification.builder()
                .sender("billing@company.com")
                .recipients("customer@example.com")
                .ccRecipients("accounting@company.com")
                .subject("Monthly Billing Invoice #1001")
                .status("SUBMITTED")
                .createdAt(now.minus(2, ChronoUnit.DAYS))
                .submittedAt(now.minus(2, ChronoUnit.DAYS))
                .build();

        notif2 = Notification.builder()
                .sender("support@company.com")
                .recipients("user@domain.org")
                .bccRecipients("audit@company.com")
                .subject("Support Ticket Update #502")
                .status("FAILED")
                .failureReason("Mailbox not found")
                .createdAt(now.minus(1, ChronoUnit.DAYS))
                .build();

        notif3 = Notification.builder()
                .sender("alerts@company.com")
                .recipients("devops@company.com, customer@example.com")
                .subject("System Maintenance Notice")
                .status("PENDING")
                .createdAt(now)
                .build();

        notificationRepository.save(notif1);
        notificationRepository.save(notif2);
        notificationRepository.save(notif3);
    }

    @Test
    void testSaveAndFindNotification() {
        Notification notification = Notification.builder()
                .sender("sender@example.com")
                .recipients("recipient1@example.com, recipient2@example.com")
                .subject("Welcome Email")
                .status("SUBMITTED")
                .createdAt(Instant.now())
                .submittedAt(Instant.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        assertNotNull(saved);
        assertNotNull(saved.getId());

        Optional<Notification> found = notificationRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("sender@example.com", found.get().getSender());
        assertEquals("Welcome Email", found.get().getSubject());
        assertEquals("SUBMITTED", found.get().getStatus());
        assertEquals("recipient1@example.com, recipient2@example.com", found.get().getRecipients());
    }

    @Test
    void testSaveFailureNotification() {
        Notification notification = Notification.builder()
                .sender("sender@example.com")
                .recipients("recipient@example.com")
                .subject("System Notification")
                .status("FAILED")
                .failureReason("SMTP connection timeout")
                .createdAt(Instant.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        assertNotNull(saved.getId());

        Optional<Notification> found = notificationRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("FAILED", found.get().getStatus());
        assertEquals("SMTP connection timeout", found.get().getFailureReason());
        assertNull(found.get().getSubmittedAt());
    }

    @Test
    void testFindAllPaginated() {
        Page<Notification> page = notificationRepository.findAll(PageRequest.of(0, 2));

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(2, page.getTotalPages());
    }
}
