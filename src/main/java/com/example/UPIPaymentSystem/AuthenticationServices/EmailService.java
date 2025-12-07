package com.example.UPIPaymentSystem.AuthenticationServices;
//using :

//JavaMailSender
//JavaMailSender
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


//We can Use JavaMailSender Service Built in service in Spring Boot to send email

//Spring Boot (JavaMailSender) sends your email to your configured SMTP server
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        
//        An SMTP provider is a service that uses the Simple Mail Transfer Protocol (SMTP) to send emails on behalf of users or applications
        mailSender.send(message);
    }
}