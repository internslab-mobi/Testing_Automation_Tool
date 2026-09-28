package xyz.mobi.testingautomationtool.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
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


    @Async
    @Override
    public void sendBugAssignedEmail(
            String recipientEmail,
            Bug bug) {


        if (bug == null
                || recipientEmail == null
                || recipientEmail.isBlank()) {

            return;
        }

        TestCase testCase = bug.getTestCase();

        TestingExecution testingExecution = null;

        if (testCase != null) {

            testingExecution =
                    testingExecutionRepository
                            .findByTestCase_TestcaseId(
                                    testCase.getTestcaseId()
                            )
                            .orElse(null);
        }

        StringBuilder message = new StringBuilder();

        appendHtmlHeader(
                message,
                "Bug Assigned",
                "A new bug has been assigned to you."
        );


        message.append("""
                <p style="margin: 0 0 20px 0;">
                    Hello,
                </p>

                <p style="margin: 0 0 20px 0;">
                    A bug has been assigned to you.
                    Please review the details below and take
                    the necessary action.
                </p>
                """);

        appendSectionStart(message, "Bug Details");
        appendRow(message, "Bug ID", bug.getBugFormatId());
        appendRow(message, "Title", bug.getTitle());

        appendRow(
                message,
                "Severity",
                bug.getSeverity()
        );

        appendRow(
                message,
                "Priority",
                bug.getPriority()
        );

        appendRow(
                message,
                "Status",
                bug.getStatus()
        );

        appendRow(
                message,
                "Reported By",
                bug.getReportedBy() != null
                        ? bug.getReportedBy().getUsername()
                        : null
        );

        appendRow(
                message,
                "Assigned To",
                bug.getAssignedTo() != null
                        ? bug.getAssignedTo().getUsername()
                        : null
        );

        appendSectionEnd(message);


        if (testCase != null) {

            appendSectionStart(
                    message,
                    "Test Case Details"
            );

            appendRow(
                    message,
                    "Test Case ID",
                    testCase.getTestcaseFormatId()
            );

            appendRow(
                    message,
                    "Title",
                    testCase.getTitle()
            );

            appendRow(
                    message,
                    "Test Type",
                    testCase.getTestType()
            );

            appendRow(
                    message,
                    "Priority",
                    testCase.getTestPriority()
            );

            appendRow(
                    message,
                    "Status",
                    testCase.getTestcaseStatus()
            );

            appendSectionEnd(message);
        }


        if (testingExecution != null) {

            appendSectionStart(
                    message,
                    "Testing Execution Details"
            );

            appendRow(
                    message,
                    "Automation Feasibility",
                    testingExecution.getAutomationFeasibility()
            );

            appendRow(
                    message,
                    "Execution Status",
                    testingExecution.getExecutionStatus()
            );

            appendRow(
                    message,
                    "Test Execution",
                    testingExecution.getTestExecution()
            );

            appendRow(
                    message,
                    "Test Validation",
                    testingExecution.getTestValidation()
            );

            appendRow(
                    message,
                    "Precondition",
                    testingExecution.getPrecondition()
            );

            appendRow(
                    message,
                    "Test Data",
                    testingExecution.getTestData()
            );

            appendRow(
                    message,
                    "Execution Steps",
                    testingExecution.getExecutionSteps()
            );

            appendRow(
                    message,
                    "UI Validations",
                    testingExecution.getUiValidations()
            );

            appendRow(
                    message,
                    "DB Validations",
                    testingExecution.getDbValidations()
            );

            appendRow(
                    message,
                    "Comments",
                    testingExecution.getComments()
            );

            appendRow(
                    message,
                    "Executed By",
                    testingExecution.getExecutedBy() != null
                            ? testingExecution
                              .getExecutedBy()
                              .getUsername()
                            : null
            );

            appendSectionEnd(message);
        }


        message.append("""
                <p style="margin-top: 28px;">
                    Please review the bug and take the necessary action.
                </p>
                """);


        appendHtmlFooter(message);


        sendHtmlEmail(
                recipientEmail,
                "Bug Assigned - "
                        + safeValue(bug.getBugFormatId()),
                message.toString()
        );
    }


    @Async
    @Override
    public void sendBugReassignedEmail(
            String recipientEmail,
            Bug bug) {


        if (bug == null
                || recipientEmail == null
                || recipientEmail.isBlank()) {

            return;
        }


        StringBuilder message = new StringBuilder();

        appendHtmlHeader(
                message,
                "Bug Reassigned",
                "This bug has been reassigned to another developer."
        );

        message.append("""
                <p style="margin: 0 0 20px 0;">
                    Hello,
                </p>

                <p style="margin: 0 0 20px 0;">
                    Your previously assigned bug has been reassigned
                    to another developer.
                </p>
                """);

        appendSectionStart(
                message,
                "Bug Details"
        );

        appendRow(
                message,
                "Bug ID",
                bug.getBugFormatId()
        );

        appendRow(
                message,
                "Title",
                bug.getTitle()
        );

        appendRow(
                message,
                "Severity",
                bug.getSeverity()
        );

        appendRow(
                message,
                "Priority",
                bug.getPriority()
        );

        appendRow(
                message,
                "Status",
                bug.getStatus()
        );

        appendRow(
                message,
                "Reassigned To",
                bug.getAssignedTo() != null
                        ? bug.getAssignedTo().getUsername()
                        : null
        );

        appendSectionEnd(message);


        message.append("""
                <p style="
                    margin-top: 28px;
                    padding: 15px;
                    background-color: #fff7ed;
                    border-left: 4px solid #f97316;
                    border-radius: 4px;
                ">
                    You no longer need to work on this bug.
                </p>
                """);


        appendHtmlFooter(message);


        sendHtmlEmail(
                recipientEmail,
                "Bug Reassigned - "
                        + safeValue(bug.getBugFormatId()),
                message.toString()
        );
    }


    private void appendHtmlHeader(
            StringBuilder message,
            String title,
            String subtitle) {

        message.append("""
                <!DOCTYPE html>

                <html>

                <head>

                    <meta charset="UTF-8">

                    <meta
                        name="viewport"
                        content="width=device-width, initial-scale=1.0"
                    >

                </head>

                <body style="
                    margin: 0;
                    padding: 0;
                    background-color: #f4f6f8;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #333333;
                ">

                    <div style="
                        max-width: 700px;
                        margin: 30px auto;
                        background-color: #ffffff;
                        border-radius: 10px;
                        overflow: hidden;
                        border: 1px solid #e5e7eb;
                    ">

                        <div style="
                            background-color: #1f2937;
                            color: #ffffff;
                            padding: 25px 30px;
                        ">

                            <div style="
                                font-size: 22px;
                                font-weight: bold;
                                margin-bottom: 7px;
                            ">
                """);

        message.append(
                escapeHtml(title)
        );

        message.append("""
                            </div>

                            <div style="
                                font-size: 14px;
                                color: #d1d5db;
                            ">
                """);

        message.append(
                escapeHtml(subtitle)
        );

        message.append("""
                            </div>

                        </div>

                        <div style="
                            padding: 30px;
                        ">
                """);
    }



    private void appendSectionStart(
            StringBuilder message,
            String title) {

        message.append("""
                <div style="
                    margin-top: 22px;
                    border: 1px solid #e5e7eb;
                    border-radius: 8px;
                    overflow: hidden;
                ">

                    <div style="
                        background-color: #f8fafc;
                        padding: 14px 18px;
                        font-size: 16px;
                        font-weight: bold;
                        color: #1f2937;
                        border-bottom: 1px solid #e5e7eb;
                    ">
                """);

        message.append(
                escapeHtml(title)
        );

        message.append("""
                    </div>

                    <div style="
                        padding: 15px 18px;
                    ">
                """);
    }


    private void appendSectionEnd(
            StringBuilder message) {

        message.append("""
                    </div>

                </div>
                """);
    }


    private void appendRow(
            StringBuilder message,
            String label,
            Object value) {

        message.append("""
                <div style="
                    display: table;
                    width: 100%;
                    border-bottom: 1px solid #f1f5f9;
                    padding: 9px 0;
                ">

                    <div style="
                        display: table-cell;
                        width: 190px;
                        font-weight: bold;
                        color: #64748b;
                        vertical-align: top;
                        padding-right: 10px;
                    ">
                """);

        message.append(
                escapeHtml(label)
        );

        message.append("""
                    </div>

                    <div style="
                        display: table-cell;
                        color: #1e293b;
                        vertical-align: top;
                        word-break: break-word;
                    ">
                """);

        message.append(
                escapeHtml(
                        safeValue(value)
                )
        );

        message.append("""
                    </div>

                </div>
                """);
    }


    private void appendHtmlFooter(
            StringBuilder message) {

        message.append("""
                        </div>

                        <div style="
                            background-color: #f8fafc;
                            padding: 20px 30px;
                            color: #64748b;
                            font-size: 13px;
                            border-top: 1px solid #e5e7eb;
                        ">

                            Regards,<br>

                            <strong>
                                Testing Automation Tool
                            </strong>

                        </div>

                    </div>

                </body>

                </html>
                """);
    }


    private void sendHtmlEmail(
            String recipientEmail,
            String subject,
            String content) {

        try {

            MimeMessage mail =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            mail,
                            false,
                            "UTF-8"
                    );

            helper.setFrom(fromEmail);

            helper.setTo(recipientEmail);

            helper.setSubject(subject);

            // true = HTML email
            helper.setText(
                    content,
                    true
            );

            mailSender.send(mail);

        } catch (MessagingException | MailException ex) {

            throw new RuntimeException(
                    "Failed to send email",
                    ex
            );
        }
    }

    private String safeValue(
            Object value) {

        return value != null
                ? String.valueOf(value)
                : "N/A";
    }


    private String escapeHtml(
            String value) {

        if (value == null) {
            return "N/A";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}