package com.chitas.example.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.chitas.example.model.FACode;
import com.chitas.example.model.Fingerprint;

@Service
@Log4j2
public class MailService {

    private final JavaMailSender emailSender;
    private final TwoFactorService twoFactorService;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000; // 2 seconds

    public MailService(JavaMailSender emailSender, TwoFactorService twoFactorService) {
        this.emailSender = emailSender;
        this.twoFactorService = twoFactorService;
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("pidaras@chitas.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                log.info("Sending email to {} with subject: {}", to, subject);
                emailSender.send(message);
                log.info("Email sent successfully to {}", to);
                return; // Success, exit
            } catch (MailException e) {
                attempts++;
                log.warn("Failed attempt {}/{} to send email to {}. Error: {}", attempts, MAX_RETRIES, to, e.getMessage());
                if (attempts == MAX_RETRIES) {
                    log.error("Exhausted retries sending email to {}", to, e);
                    throw new RuntimeException("Failed to send email after " + MAX_RETRIES + " attempts", e);
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS); // Wait before retry
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Retry interrupted for email to {}", to, ie);
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
    }

    public void sendVerficationCode(Fingerprint fing) {
        FACode code = twoFactorService.generateCode(fing);
        sendSimpleMessage(fing.getUser().getEmail(), "VERIFACTION CODE", code.getCode());
    }
}