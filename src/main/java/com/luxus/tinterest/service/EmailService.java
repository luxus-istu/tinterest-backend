package com.luxus.tinterest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Async("emailTaskExecutor")
    public void sendCode(String to, String code) {
        if (!mailEnabled) {
            log.info("Mail delivery is disabled. Verification code for {}: {}", to, code);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Verification code");
        message.setText("Your verification code: " + code);

        mailSender.send(message);
    }
}
