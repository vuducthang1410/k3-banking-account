package com.system.common_library.dto.notifcation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTP implements Serializable {
    private String otp;
    private LocalDateTime expiredTime;
//    public String getExpiredTime() {
//        if (expiredTime == null) {
//            return "No expiration time set";
//        }
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
//        return expiredTime.format(formatter);
//    }
}
