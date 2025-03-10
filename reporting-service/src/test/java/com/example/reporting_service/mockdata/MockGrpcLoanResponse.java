package com.example.reporting_service.mockdata;

import com.system.common_library.dto.report.LoanReportResponse;
import com.system.common_library.dto.response.loan.PaymentScheduleRp;
import com.system.common_library.enums.FormDeftRepaymentEnum;
import com.system.common_library.enums.LoanStatus;
import com.system.common_library.enums.Unit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MockGrpcLoanResponse {
    public static LoanReportResponse getSingleMockLoan() {
        return LoanReportResponse.builder()
                .customerId("CUST001")
                .loanId("LOAN123456")
                .loanAmount(50000000.0)
                .loanType(FormDeftRepaymentEnum.PRINCIPAL_AND_INTEREST_MONTHLY)
                .startDate(LocalDate.of(2023, 1, 15))
                .loanStatus(LoanStatus.ACTIVE)
                .remainingBalance(30000000.0)
                .paymentScheduleList(Collections.singletonList(getMockPaymentSchedule()))
                .interestRate(5.5)
                .unit(Unit.DATE)
                .accountLoanNumber("ACCLOAN123")
                .build();
    }

    public static PaymentScheduleRp getMockPaymentSchedule() {
        return new PaymentScheduleRp(
                "PAY001",
                "Thanh toán kỳ 1",
                "2025-03-10",
                "5000000",
                "Chưa thanh toán",
                "false"
        );
    }

    private static final FormDeftRepaymentEnum[] LOAN_TYPES = FormDeftRepaymentEnum.values();
    private static final LoanStatus[] LOAN_STATUSES = LoanStatus.values();
    private static final Unit[] UNITS = Unit.values();
    private static final Random RANDOM = new Random();

    public static List<LoanReportResponse> generateMockLoans(int count) {
        List<LoanReportResponse> loans = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            loans.add(LoanReportResponse.builder()
                    .customerId("CUST" + String.format("%03d", i))
                    .loanId("LOAN" + (1000 + i))
                    .loanAmount(5000000.0 * (i % 20 + 1)) // Khoản vay tăng dần
                    .loanType(LOAN_TYPES[i % LOAN_TYPES.length])
                    .startDate(LocalDate.of(2020 + i % 5, 1 + i % 12, 1 + i % 28))
                    .loanStatus(LOAN_STATUSES[i % LOAN_STATUSES.length])
                    .remainingBalance(2000000.0 * (i % 10 + 1))
                    .paymentScheduleList(generateMockPaymentSchedule(i % 5 + 1))
                    .interestRate(4.0 + (i % 5) * 0.5)
                    .unit(UNITS[i % UNITS.length])
                    .accountLoanNumber("ACCLOAN" + (2000 + i))
                    .build());
        }
        return loans;
    }

    public static List<PaymentScheduleRp> generateMockPaymentSchedule(int count) {
        List<PaymentScheduleRp> schedules = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            schedules.add(new PaymentScheduleRp(
                    "PAY" + (1000 + i),
                    "Thanh toán kỳ " + i,
                    LocalDate.now().plusMonths(i).toString(),
                    String.valueOf(500000 * (i % 5 + 1)),
                    i % 2 == 0 ? "Đã thanh toán" : "Chưa thanh toán",
                    i % 2 == 0 ? "true" : "false"
            ));
        }
        return schedules;
    }
}
