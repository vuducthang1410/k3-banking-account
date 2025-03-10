package com.example.reporting_service.model.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportTitle {
    ACCOUNT_REPORT("BÁO CÁO TÀI KHOẢN"),
    TRANSACTION_REPORT("BÁO CÁO GIAO DỊCH"),
    LOAN_REPORT("BÁO CÁO KHOẢN VAY"),
    LOAN_PAYMENT_SCHEDULE("Lịch thanh toán của khoản vay dự kiến"),
    TRANSACTION_TABLE("DANH SÁCH GIAO DỊCH");

    private final String title;
}
