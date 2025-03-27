package com.system.customer_service.service;

import com.system.customer_service.entity.Province;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface ProvinceService {
    String getProvinceNumber(String province);
    boolean checkIdentityNumber(String identityNumber, String placeOrigin, LocalDate dateOfBirth, String gender);
    List<Province> getAllProvince();
}
