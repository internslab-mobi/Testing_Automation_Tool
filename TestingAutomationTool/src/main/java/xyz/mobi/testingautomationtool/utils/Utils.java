package xyz.mobi.testingautomationtool.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingAuditLog;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.repository.AuditLogRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class Utils {
    private final AuditLogRepository AuditLogRepository;

    public void trigger(TestCase testCase, User user, Bug bug){

        try{

            TestingAuditLog testingAuditLog = new TestingAuditLog();
            testingAuditLog.setTestCase(testCase);
            testingAuditLog.setExecutedAt(LocalDateTime.now());
            testingAuditLog.setTestcaseStatus(testCase.getTestcaseStatus());
            testingAuditLog.setBug(bug);

            if(bug != null){
                testingAuditLog.setBugStatus(bug.getStatus());
            }
            else{
                testingAuditLog.setBugStatus(null);
            }

            testingAuditLog.setExecutedBy(user.getUserId());

            AuditLogRepository.save(testingAuditLog);
        }catch (Exception e){
            throw new IllegalArgumentException(
                    "Error while adding value in the table:"+e.getMessage());
        }
    }
}
