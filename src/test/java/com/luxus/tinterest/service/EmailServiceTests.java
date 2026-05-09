package com.luxus.tinterest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Unit Tests")
class EmailServiceTests {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() throws Exception {
        Field mailEnabledField = EmailService.class.getDeclaredField("mailEnabled");
        mailEnabledField.setAccessible(true);
        mailEnabledField.set(emailService, false);

        Field fromField = EmailService.class.getDeclaredField("from");
        fromField.setAccessible(true);
        fromField.set(emailService, "noreply@example.com");
    }

    @Test
    @DisplayName("Should not send email when mail delivery is disabled")
    void testSendCodeDoesNotSendWhenDisabled() {
        emailService.sendCode("user@example.com", "123456");

        verifyNoInteractions(mailSender);
    }

    @Test
    @DisplayName("Should send email message when mail delivery is enabled")
    void testSendCodeSendsEmailWhenEnabled() throws Exception {
        Field mailEnabledField = EmailService.class.getDeclaredField("mailEnabled");
        mailEnabledField.setAccessible(true);
        mailEnabledField.set(emailService, true);

        emailService.sendCode("user@example.com", "123456");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals("noreply@example.com", message.getFrom());
        assertArrayEquals(new String[]{"user@example.com"}, message.getTo());
        assertEquals("Verification code", message.getSubject());
        assertEquals("Your verification code: 123456", message.getText());
    }
}
