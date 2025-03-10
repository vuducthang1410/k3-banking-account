package com.system.customer_service.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public interface ProvinceService {
    String getProvinceNumber(String province);
    boolean checkIdentityNumber(String identityNumber, String placeOrigin, LocalDate dateOfBirth, String gender);
}
