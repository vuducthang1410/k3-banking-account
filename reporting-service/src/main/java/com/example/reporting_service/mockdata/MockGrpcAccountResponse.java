package com.example.reporting_service.mockdata;

import com.system.common_library.dto.report.AccountReportResponse;
import com.system.common_library.enums.AccountType;
import com.system.common_library.enums.ObjectStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MockGrpcAccountResponse {
    public static List<AccountReportResponse> generateMockAccountReports(int count) {
        List<AccountReportResponse> reports = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            reports.add(AccountReportResponse.builder()
                    .customerId("CUST" + String.format("%03d", i))
                    .accountNumber("10000000" + i)
                    .accountType(i % 2 == 0 ? AccountType.SAVINGS : AccountType.CREDIT)
                    .status(i % 3 == 0 ? ObjectStatus.SUSPENDED : ObjectStatus.ACTIVE)
                    .bankBranch(i % 2 == 0 ? "Hà Nội" : "TP HCM")
                    .balance(1000000.0 * i)
                    .openedAt(LocalDateTime.now().minusMonths(i))
                    .creditLimit(String.valueOf(5000000 * (i % 10)))
                    .debtBalance(String.valueOf(100000 * (i % 5)))
                    .rate(3.0 + (i % 5) * 0.5)
                    .billingCycle(30 + (i % 3) * 15)
                    .build());
        }
        return reports;
    }

    public static AccountReportResponse getSingleMockAccountReport() {
        return AccountReportResponse.builder()
                .customerId("CUST001")
                .accountNumber("123456789")
                .accountType(AccountType.SAVINGS)
                .status(ObjectStatus.ACTIVE)
                .bankBranch("Hà Nội")
                .balance(5000000.0)
                .openedAt(LocalDateTime.now().minusYears(2))
                .creditLimit("50000000")
                .debtBalance("1000000")
                .rate(3.5)
                .billingCycle(30)
                .build();
    }

}
