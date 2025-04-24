package com.jacto.scheduler.service;

public interface MailService {
    void sendEmail(String to, String subject, String text);
    void sendEmailWithAttachment(String to, String subject, String text, String attachmentPath);
}
