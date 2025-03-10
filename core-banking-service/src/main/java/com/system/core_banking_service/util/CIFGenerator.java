package com.system.core_banking_service.util;

import com.system.common_library.enums.Gender;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Random;

public class CIFGenerator implements Serializable {

    public static String generateCIFCode(Gender gender, LocalDate date, String phone) {

        StringBuilder sb = new StringBuilder();

        // First 4 digits depend on date of birth
        String day = String.valueOf(date.getDayOfMonth());
        sb.append(day, day.length() - 1, day.length());
        String month = String.valueOf(date.getMonthValue());
        sb.append(month, month.length() - 1, month.length());
        String year = String.valueOf(date.getYear());
        sb.append(year, year.length() - 2, year.length());

        // Next digit depends on gender
        sb.append(gender.equals(Gender.FEMALE) ? "0" : "1");

        // Next 3 digits depend on the last 3 digits of the phone number
        sb.append(phone, phone.length() - 3, phone.length());

        // Last 3 digit are random
        for (int i = 0; i < 3; i++) {

            // Generate random digit within 0-9
            int ranNo = new Random().nextInt(9);
            sb.append(ranNo);
        }

        return sb.toString();
    }
}
