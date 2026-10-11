package xyz.mobi.testingautomationtool.service;

public interface EmailService {
    void sendPasswordResetOtpEmail(String toEmail, String username, String otpCode, int expiryMinutes);
}
