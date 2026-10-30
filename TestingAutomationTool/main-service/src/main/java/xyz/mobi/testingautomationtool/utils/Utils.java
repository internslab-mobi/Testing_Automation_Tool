package xyz.mobi.testingautomationtool.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingAuditLog;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.repository.AuditLogRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class Utils {
    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trigger(TestCase testCase, User user, Bug bug){
        try{
            TestingAuditLog testingAuditLog = new TestingAuditLog();
            testingAuditLog.setTestCase(testCase);
            testingAuditLog.setExecutedAt(testCase.getUpdatedAt() != null ? testCase.getUpdatedAt() : java.time.Instant.now());
            testingAuditLog.setTestcaseStatus(testCase.getTestcaseStatus());
            testingAuditLog.setBug(bug);
            testingAuditLog.setExecutedBy(user != null ? user.getUserId() : (testCase.getCreatedBy() != null ? testCase.getCreatedBy().getUserId() : 1));

            auditLogRepository.save(testingAuditLog);
        } catch (Exception e){
            log.error("Failed to record testcase audit log for testcase ID {}: {}",
                    testCase != null ? testCase.getTestcaseId() : null, e.getMessage(), e);
        }
    }
}