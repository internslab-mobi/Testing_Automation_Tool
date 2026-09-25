package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;
import xyz.mobi.testingautomationtool.repository.TestingExecutionRepository;
import xyz.mobi.testingautomationtool.service.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    private final TestingExecutionRepository testingExecutionRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendBugAssignmentEmail(
            String to,
            Bug bug) {

        // Get testcase connected to the bug
        TestCase testCase = bug.getTestCase();

        // Get execution details connected to the testcase
        TestingExecution testingExecution =
                testingExecutionRepository
                        .findByTestCase(testCase)
                        .orElse(null);

        StringBuilder message = new StringBuilder();

        message.append("Hello,\n\n");

        message.append("A bug has been assigned to you.\n\n");

        // =====================================================
        // BUG DETAILS
        // =====================================================

        message.append("====================================\n");
        message.append("            BUG DETAILS\n");
        message.append("====================================\n\n");

        message.append("Bug ID        : ")
                .append(safeValue(bug.getBugFormatId()))
                .append("\n");

        message.append("Database ID   : ")
                .append(safeValue(bug.getBugId()))
                .append("\n");

        message.append("Title         : ")
                .append(safeValue(bug.getTitle()))
                .append("\n");

        message.append("Description   : ")
                .append(safeValue(bug.getDescription()))
                .append("\n");

        message.append("Severity      : ")
                .append(safeValue(bug.getSeverity()))
                .append("\n");

        message.append("Priority      : ")
                .append(safeValue(bug.getPriority()))
                .append("\n");

        message.append("Status        : ")
                .append(safeValue(bug.getStatus()))
                .append("\n");

        message.append("Occurrence    : ")
                .append(safeValue(bug.getBugOccurrence()))
                .append("\n");

        message.append("Reported By   : ")
                .append(
                        bug.getReportedBy() != null
                                ? safeValue(
                                bug.getReportedBy().getUsername())
                                : "N/A"
                )
                .append("\n");

        message.append("Assigned To   : ")
                .append(
                        bug.getAssignedTo() != null
                                ? safeValue(
                                bug.getAssignedTo().getUsername())
                                : "N/A"
                )
                .append("\n");

        message.append("Resolved At   : ")
                .append(safeValue(bug.getResolvedAt()))
                .append("\n\n");

        // =====================================================
        // TEST CASE DETAILS
        // =====================================================

        message.append("====================================\n");
        message.append("          TEST CASE DETAILS\n");
        message.append("====================================\n\n");

        if (testCase != null) {

            message.append("Test Case ID  : ")
                    .append(
                            safeValue(
                                    testCase.getTestcaseFormatId()))
                    .append("\n");

            message.append("Database ID   : ")
                    .append(
                            safeValue(
                                    testCase.getTestcaseId()))
                    .append("\n");

            message.append("Title         : ")
                    .append(
                            safeValue(
                                    testCase.getTitle()))
                    .append("\n");

            message.append("Test Type     : ")
                    .append(
                            safeValue(
                                    testCase.getTestType()))
                    .append("\n");

            message.append("Priority      : ")
                    .append(
                            safeValue(
                                    testCase.getTestPriority()))
                    .append("\n");

            message.append("Status        : ")
                    .append(
                            safeValue(
                                    testCase.getTestcaseStatus()))
                    .append("\n");

            message.append("Active        : ")
                    .append(
                            safeValue(
                                    testCase.isActive()))
                    .append("\n\n");

        } else {
            message.append("Testcase details are unavailable.\n\n");
        }

        // =====================================================
        // TESTING EXECUTION DETAILS
        // =====================================================

        message.append("====================================\n");
        message.append("       TESTING EXECUTION DETAILS\n");
        message.append("====================================\n\n");

        if (testingExecution != null) {

            message.append("Execution ID       : ")
                    .append(
                            safeValue(
                                    testingExecution.getExecutionId()))
                    .append("\n");

            message.append("Execution Number   : ")
                    .append(
                            safeValue(
                                    testingExecution.getExecutionNumber()))
                    .append("\n");

            message.append("Automation Status  : ")
                    .append(
                            safeValue(
                                    testingExecution
                                            .getAutomationFeasibility()))
                    .append("\n");

            message.append("Execution Status   : ")
                    .append(
                            safeValue(
                                    testingExecution
                                            .getExecutionStatus()))
                    .append("\n");

            message.append("Test Execution     : ")
                    .append(
                            safeValue(
                                    testingExecution
                                            .getTestExecution()))
                    .append("\n\n");

            message.append("Test Validation    : ")
                    .append(
                            safeValue(
                                    testingExecution
                                            .getTestValidation()))
                    .append("\n\n");

            message.append("Precondition       : ")
                    .append(
                            safeValue(
                                    testingExecution
                                            .getPrecondition()))
                    .append("\n\n");

            message.append("Test Data          : ")
                    .append(
                            safeValue(
                                    testingExecution
                                            .getTestData()))
                    .append("\n\n");

            message.append("Execution Steps    : ")
                    .append(
                            safeValue(
                                    testingExecution
                                            .getExecutionSteps()))
                    .append("\n\n");

            message.append("UI Validations     : ")
                    .append(
                            safeValue(
                                    testingExecution
                                            .getUiValidations()))
                    .append("\n\n");

            message.append("DB Validations     : ")
                    .append(
                            safeValue(
                                    testingExecution
                                            .getDbValidations()))
                    .append("\n\n");

            message.append("Comments           : ")
                    .append(
                            safeValue(
                                    testingExecution
                                            .getComments()))
                    .append("\n\n");

            message.append("Executed At        : ")
                    .append(
                            safeValue(
                                    testingExecution
                                            .getExecutedAt()))
                    .append("\n");

            message.append("Executed By        : ")
                    .append(
                            testingExecution.getExecutedBy() != null
                                    ? safeValue(
                                    testingExecution
                                    .getExecutedBy()
                                    .getUsername())
                                    : "N/A"
                    )
                    .append("\n\n");

        } else {
            message.append(
                    "Testing execution details are unavailable.\n\n");
        }

        message.append("====================================\n");
        message.append("Please review the bug and take the necessary action.\n\n");

        message.append("Regards,\n");
        message.append("Testing Automation Tool");

        // =====================================================
        // SEND EMAIL
        // =====================================================

        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setFrom(fromEmail);
        mail.setTo(to);
        mail.setSubject(
                "Bug Assigned - " + bug.getBugFormatId());
        mail.setText(message.toString());

        mailSender.send(mail);
    }

    private String safeValue(Object value) {
        return value != null
                ? String.valueOf(value)
                : "N/A";
    }
}