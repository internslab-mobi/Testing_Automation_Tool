package xyz.mobi.testingautomationtool.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
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
    public void confirmationEmail(
            String recipientEmail,
            String username) {

        try {
            MimeMessage mail = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(mail, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);

            helper.setSubject("User Account Created Successfully");

            String content = """
                    <html>
                    <body>
                        <p>Dear User,</p>
                    
                        <p>
                            Your user account has been successfully created.
                        </p>
                    
                        <p>
                            <strong>Username:</strong> %s<br>
                            <strong>Email:</strong> %s
                        </p>
                    
                        <p>
                            You can now access the Testing Automation Tool
                            using your registered credentials.
                        </p>
                    
                        <p>
                            For security reasons, please do not share your
                            password with anyone.
                        </p>
                    
                        <p>
                            Regards,<br>
                            Testing Automation Tool Team
                        </p>
                    </body>
                    </html>
                    """.formatted(username, recipientEmail);

            helper.setText(content, true);

            mailSender.send(mail);

        } catch (MessagingException | MailException ex) {
            throw new RuntimeException("Failed to send email", ex);
        }
    }

    @Override
    public void rejectEmail(String recipientEmail, String username) {

        try {
            MimeMessage mail = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(mail, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);

            helper.setSubject("User Account Registration Rejected");

            String content = """
            <html>
            <body>
                <p>Dear User,</p>
    
                <p>
                    Your user account registration request has been rejected
                    by the administrator.
                </p>
    
                <p>
                    <strong>Username:</strong> %s<br>
                    <strong>Email:</strong> %s
                </p>
    
                <p>
                    You will not be able to access the Testing Automation Tool
                    using this account.
                </p>
    
                <p>
                    If you believe this rejection was made in error or require
                    further clarification, please contact the administrator.
                </p>
    
                <p>
                    Regards,<br>
                    Testing Automation Tool Team
                </p>
            </body>
            </html>
            """.formatted(username, recipientEmail);

            helper.setText(content, true);

            mailSender.send(mail);

        } catch (MessagingException | MailException ex) {
            throw new RuntimeException("Failed to send email", ex);
        }
    }
}