package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.entity.Bug;

public interface EmailService {

    void sendBugAssignedEmail(String recipientEmail, Bug bug);

    void sendBugReassignedEmail(String recipientEmail, Bug bug);

    void confirmationEmail(String toEmail, String username);

    void rejectEmail(String toEmail, String username);
}
