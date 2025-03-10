package com.example.notification_service.service.interfaces;

public interface SMSRegistationService {
    void registSMSService(String customerCIF, String phoneNumber);
    void unRegistSMSService(String customerCIF, String phoneNumber) ;
    void updatePhoneNumber(String customerCIF, String newPhoneNumber);
    boolean checkCustomerRegistration(String customerCIF) ;
}
