package com.example.notification_service.service.interfaces;

public interface EmailSendingService {
    void sendEmail(String to, String subject, String body) ;
    Boolean sendHTMLEmail(String to, String subject, String htmlContent);
}
