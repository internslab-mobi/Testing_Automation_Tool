package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.entity.Bug;

public interface EmailService {

    void sendBugAssignmentEmail(
            String to,
            Bug bug);
}
