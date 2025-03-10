package com.example.notification_service.service;

import com.example.notification_service.service.interfaces.EmailSendingService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.util.MailConnectException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendingServiceImpl implements EmailSendingService {
    @Value("${spring.mail.username}")
    private String emailSenderAddress;

    private final JavaMailSender javaMailSender;
    @Override
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailSenderAddress);
        message.setTo(to);
        message.setText(body);
        message.setSubject(subject);
        javaMailSender.send(message);
    }
    @Override
    public Boolean sendHTMLEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true,"UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = is HTML
            javaMailSender.send(message);
            log.info("Email sent successfully to {}", to);
            return true;
        } catch (MailSendException e) {
            if (e.getCause() instanceof MailConnectException) {
                System.err.println("Failed to connect to the mail server: " + e.getCause().getMessage());
            } else {
                System.err.println("Failed to send email: " + e.getMessage());
            }
        } catch (MailConnectException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                System.err.println("Connection to the mail server timed out: " + e.getCause().getMessage());
            } else {
                System.err.println("Could not connect to the mail server: " + e.getMessage());
            }
        } catch (MessagingException e) {
            System.err.println("Error while creating the email message: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while sending the email: " + e.getMessage());
        }
        return false;
    }

}
