package com.system.transaction_service.util;

import java.io.Serializable;
import java.util.Random;

public class OTPGenerator implements Serializable {
    
    public static String generateOTP(int len) {

        String otp = "";
        for (int i = 0; i < len; i++) {

            // Generate random digit within 0-9
            int ranNo = new Random().nextInt(9);
            otp = otp.concat(Integer.toString(ranNo));
        }

        return otp;
    }
}
