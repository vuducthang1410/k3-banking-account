package com.example.notification_service.service;

import com.example.notification_service.domain.entity.SMSRegistration;
import com.example.notification_service.repository.SMSRegistationRepository;
import com.example.notification_service.service.interfaces.SMSRegistationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SMSRegistationServiceImpl implements SMSRegistationService {
    private final SMSRegistationRepository smsRegistrationRepository;
    @Override
    public void registSMSService(String customerCIF, String phoneNumber) {
        Optional<SMSRegistration> existingRegistration = smsRegistrationRepository.findById(customerCIF);

        if (existingRegistration.isPresent()) {
            SMSRegistration smsRegistration = existingRegistration.get();
            if (smsRegistration.getRegistered() && smsRegistration.getPhoneNumber().equals(phoneNumber)) { // If already registered and phone number matches, do nothing
                return;
            }
            if (!smsRegistration.getRegistered()) {// If regist is false, re-register the customer
                smsRegistration.setRegistered(true);
                smsRegistration.setUnregisterAt(null);
            }
            if (!smsRegistration.getPhoneNumber().equals(phoneNumber)) {// If phone number is different, update it
                smsRegistration.setPhoneNumber(phoneNumber);
            }
            smsRegistrationRepository.save(smsRegistration);
        } else {
            SMSRegistration newRegistration = SMSRegistration.builder()
                    .customerCIF(customerCIF)
                    .phoneNumber(phoneNumber)
                    .registered(true)
                    .unregisterAt(null)
                    .build();
            smsRegistrationRepository.save(newRegistration);
        }
    }
    @Override
    public void unRegistSMSService(String customerCIF, String phoneNumber) {
        Optional<SMSRegistration> smsRegistrationOptional = smsRegistrationRepository.findById(customerCIF);
        if (smsRegistrationOptional.isEmpty()) {
            throw new EntityNotFoundException("Customer CIF not found.");
        }
        SMSRegistration smsRegistration = smsRegistrationOptional.get();
        if (!smsRegistration.getRegistered()) {
            throw new IllegalStateException("Service is already unregistered.");
        }
        smsRegistration.setRegistered(false);
        smsRegistration.setUnregisterAt(LocalDateTime.now());
        smsRegistrationRepository.save(smsRegistration);
    }
    @Override
    public void updatePhoneNumber(String customerCIF, String newPhoneNumber) {
        // Find the record by customer CIF
        SMSRegistration smsRegistration = smsRegistrationRepository.findById(customerCIF)
                .orElseThrow(() -> new EntityNotFoundException("Customer CIF not found."));
        smsRegistration.setPhoneNumber(newPhoneNumber);// Update phone number
        smsRegistrationRepository.save(smsRegistration);
    }
    @Override
    public boolean checkCustomerRegistration(String customerCIF) {
        Optional<SMSRegistration> smsRegistrationOptional = smsRegistrationRepository.findById(customerCIF);
        if (smsRegistrationOptional.isEmpty()){
            return false;
        }
        else if (!smsRegistrationOptional.get().getRegistered()) {
            return false;
        }
        return true;
    }
}
