package com.khaldoun.ecommerce.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.khaldoun.ecommerce.auth.config.AppProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    public EmailService(JavaMailSender mailSender, AppProperties appProperties) {
        this.mailSender = mailSender;
        this.appProperties = appProperties;
    }

    public void sendConfirmationEmail(String toEmail, String confirmationToken) {
        String confirmationUrl = appProperties.frontend().confirmUrl() + "?token=" + confirmationToken;
        String html = "<h1>Confirm your email</h1>"
                + "<p>Please confirm your email address by clicking the link below.</p>"
                + "<p><a href=\"" + confirmationUrl + "\">Confirm email</a></p>";

        log.info("Email confirmation URL for {}: {}", toEmail, confirmationUrl);
        sendHtmlEmail(toEmail, "Confirm your email", html);
    }

    public void sendWelcomeEmail(String toEmail) {
        String html = "<h1>Welcome!</h1>"
                + "<p>Your email is confirmed and your account is ready to use.</p>";
        try {
            sendHtmlEmail(toEmail, "Welcome to our store", html);
        } catch (MailException exception) {
            // Non-critical: confirmation already succeeded, don't fail the request/transaction over this
            log.warn("Failed to send welcome email to {}: {}", toEmail, exception.getMessage());
        }
    }

    private void sendHtmlEmail(String toEmail, String subject, String html) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException exception) {
            throw new MailSendException("Failed to create email for " + toEmail, exception);
        }
    }
}