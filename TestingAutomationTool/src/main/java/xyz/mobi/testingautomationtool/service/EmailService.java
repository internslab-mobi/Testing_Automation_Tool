package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.entity.Bug;

public interface EmailService {

    void confirmationEmail(String recipientEmail,String username);

    void rejectEmail(String recipientEmail,String username);
}
