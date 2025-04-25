package com.jacto.scheduler.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender emailSender;

    @InjectMocks
    private MailService mailService;

    private String to;
    private String subject;
    private String text;

    @BeforeEach
    void setUp() {
        to = "test@example.com";
        subject = "Test Subject";
        text = "Test Message";
    }

    @Test
    void sendEmail_ShouldSendEmail() {
        // Act
        mailService.sendEmail(to, subject, text);

        // Assert
        verify(emailSender).send(any(SimpleMailMessage.class));
    }
}
