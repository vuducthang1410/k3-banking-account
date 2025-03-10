package com.system.core_banking_service.util;

import com.system.common_library.enums.AccountType;

import java.util.Random;

public class AccountNumberGenerator {

    public static String generateAccountNumber(AccountType type, String cifCode) {

        StringBuilder sb = new StringBuilder();

        // First 11 digits is customer's CIF code
        sb.append(cifCode);

        // Next digit depends on account type
        sb.append(type.getValue());;

        // Last 3 digit are random
        for (int i = 0; i < 3; i++) {

            // Generate random digit within 0-9
            int ranNo = new Random().nextInt(9);
            sb.append(ranNo);
        }

        return sb.toString();
    }
}
