package xyz.mobi.testingautomationtool.service;

import org.springframework.scheduling.annotation.Async;
import xyz.mobi.testingautomationtool.entity.Bug;

public interface EmailService {

    void sendBugAssignedEmail(String recipientEmail, Bug bug);

    void sendBugReassignedEmail(String recipientEmail, Bug bug);
}
