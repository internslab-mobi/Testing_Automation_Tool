package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;
import xyz.mobi.testingautomationtool.repository.TestingExecutionRepository;
import xyz.mobi.testingautomationtool.service.EmailService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TestingExecutionRepository testingExecutionRepository;

    @Value("${spring.mail.username:vikramraajak@gmail.com}")
    private String fromEmail;

    @Override
    public void sendBugAssignmentEmail(String toEmail, Bug bug) {
        TestCase testCase = bug.getTestCase();
        TestingExecution execution = null;
        if (testCase != null && testCase.getTestcaseId() != null) {
            execution = testingExecutionRepository
                    .findByTestCaseTestcaseId(testCase.getTestcaseId())
                    .orElse(null);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Hi,\n\n");
        sb.append("A new bug has been assigned to you in Testing Automation Tool.\n\n");

        sb.append("====================================\n");
        sb.append("            BUG DETAILS\n");
        sb.append("====================================\n");
        sb.append("Bug ID        : ").append(safe(bug.getBugFormatId())).append("\n");
        sb.append("Database ID   : ").append(safe(bug.getBugId())).append("\n");
        sb.append("Title         : ").append(safe(bug.getTitle())).append("\n");
        sb.append("Description   : ").append(safe(bug.getDescription())).append("\n");
        sb.append("Severity      : ").append(safe(bug.getSeverity())).append("\n");
        sb.append("Priority      : ").append(safe(bug.getPriority())).append("\n");
        sb.append("Category      : ").append(safe(bug.getCategory())).append("\n");
        sb.append("Status        : ").append(safe(bug.getStatus())).append("\n");
        sb.append("Occurrence    : ").append(safe(bug.getBugOccurrence())).append("\n");
        sb.append("Reported By   : ").append(bug.getReportedBy() != null ? safe(bug.getReportedBy().getUsername()) : "N/A").append("\n\n");

        sb.append("====================================\n");
        sb.append("          TEST CASE DETAILS\n");
        sb.append("====================================\n");
        if (testCase != null) {
            sb.append("Test Case ID  : ").append(safe(testCase.getTestcaseFormatId())).append("\n");
            sb.append("Title         : ").append(safe(testCase.getTitle())).append("\n");
            sb.append("Test Type     : ").append(safe(testCase.getTestType())).append("\n");
            sb.append("Priority      : ").append(safe(testCase.getTestPriority())).append("\n");
            sb.append("Status        : ").append(safe(testCase.getTestcaseStatus())).append("\n\n");
        } else {
            sb.append("No TestCase details available.\n\n");
        }

        if (execution != null) {
            sb.append("====================================\n");
            sb.append("       TEST EXECUTION DETAILS\n");
            sb.append("====================================\n");
            sb.append("Execution ID  : ").append(safe(execution.getExecutionId())).append("\n");
            sb.append("Feasibility   : ").append(safe(execution.getAutomationFeasibility())).append("\n");
            sb.append("Precondition  : ").append(safe(execution.getPrecondition())).append("\n");
            sb.append("Steps         : ").append(safe(execution.getExecutionSteps())).append("\n");
            sb.append("Validation    : ").append(safe(execution.getTestValidation())).append("\n");
            sb.append("UI Validation : ").append(safe(execution.getUiValidations())).append("\n");
            sb.append("DB Validation : ").append(safe(execution.getDbValidations())).append("\n\n");
        }

        sb.append("Please review and resolve the issue.\n\n");
        sb.append("Regards,\n");
        sb.append("Testing Automation Tool Team");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Bug Assigned: " + bug.getBugFormatId() + " - " + bug.getTitle());
        message.setText(sb.toString());

        log.info("Sending bug assignment email to {} for bug {}", toEmail, bug.getBugFormatId());
        mailSender.send(message);
    }

    private String safe(Object val) {
        return val != null ? String.valueOf(val) : "N/A";
    }
}
