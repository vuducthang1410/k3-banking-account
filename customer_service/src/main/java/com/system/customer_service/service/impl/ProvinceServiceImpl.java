package com.system.customer_service.service.impl;

import com.system.customer_service.repository.ProvinceRepository;
import com.system.customer_service.service.ProvinceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProvinceServiceImpl implements ProvinceService {
    private final ProvinceRepository provinceRepository;

    @Override
    public String getProvinceNumber(String province) {
        return provinceRepository.findNumberByName(province);
    }

    @Override
    public boolean checkIdentityNumber(String identityNumber, String placeOrigin, LocalDate dateOfBirth, String gender) {
        // Lấy mã tỉnh
        String provinceNumber = getProvinceNumber(placeOrigin);
        log.info("Province number: {}", provinceNumber);

        // Lấy năm sinh
        int year = dateOfBirth.getYear();
        log.info("Year: {}", year);

        // Lấy 2 số cuối của năm sinh
        String lastTwoNumberOfYear = String.valueOf(year).substring(2);
        log.info("Last two year number: {}", lastTwoNumberOfYear);

        // Xác định số thứ tư của CCCD dựa vào năm sinh và giới tính
        String numberFour;
        if (year >= 1900 && year <= 1999) {
            numberFour = gender.equals("Nam") ? "0" : "1";
        } else if (year >= 2000 && year <= 2099) {
            numberFour = gender.equals("Nam") ? "2" : "3";
        } else {
            return false; // Năm sinh không hợp lệ
        }
        log.info("Number four: {}", numberFour);

        // Kiểm tra xem số CCCD có đúng định dạng không
        String expectedPrefix = provinceNumber + numberFour + lastTwoNumberOfYear;
        boolean isValid = identityNumber.startsWith(expectedPrefix) && identityNumber.length() == 12 && identityNumber.matches("\\d+");

        log.info("Identity number validation result: {}", isValid);
        return isValid;
    }

}
