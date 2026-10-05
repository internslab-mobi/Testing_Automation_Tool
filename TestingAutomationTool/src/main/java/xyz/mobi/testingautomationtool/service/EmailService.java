package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.entity.Bug;

public interface EmailService {

    void sendBugAssignedEmail(String recipientEmail, Bug bug);

    void sendBugReassignedEmail(String recipientEmail, Bug bug);

    void confirmationEmail(String toEmail, String username);

    void rejectEmail(String toEmail, String username);

    void sendPasswordResetOtpEmail(String toEmail, String username, String otpCode, int expiryMinutes);

    void sendManagerCredentials(
            String email,
            String username,
            String password
    );
}
