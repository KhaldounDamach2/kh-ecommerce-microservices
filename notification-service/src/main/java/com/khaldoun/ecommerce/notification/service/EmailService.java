package com.khaldoun.ecommerce.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    // Best effort: the in-app notification is already saved, so a failed
    // email is logged and never breaks the Kafka processing.
    public void send(String toEmail, String subject, String text) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText("<h2>" + subject + "</h2><p>" + text + "</p>", true);
            mailSender.send(mimeMessage);
            log.info("Sent email '{}' to {}", subject, toEmail);
        } catch (MessagingException | MailException exception) {
            log.warn("Failed to send email '{}' to {}: {}", subject, toEmail, exception.getMessage());
        }
    }
}
