package com.chitas.example.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.chitas.example.model.FACode;
import com.chitas.example.model.Fingerprint;


@Service
public class MailService {

  private final JavaMailSender emailSender;
  private final TwoFactorService twoFactorService;

  public MailService(JavaMailSender emailSender, TwoFactorService twoFactorService) {
    this.emailSender = emailSender;
    this.twoFactorService = twoFactorService;

  }

  public void sendSimpleMessage(
      String to, String subject, String text) {

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("pidaras@chitas.com");
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    emailSender.send(message);

  }

  public void sendVerficationCode(Fingerprint fing) {
    
    FACode code = twoFactorService.generateCode(fing);
    sendSimpleMessage(fing.getUser().getEmail(), "VERIFACTION CODE", code.getCode());
  }

}