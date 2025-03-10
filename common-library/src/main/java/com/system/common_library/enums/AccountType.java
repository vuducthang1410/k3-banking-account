package com.system.common_library.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountType {
    PAYMENT(0, "Tài khoản thanh toán"),
    SAVINGS(1, "Tài khoản tiết kiệm"),
    LOAN(2, "Tài khoản vay"),
    CREDIT(3, "Tài khoản tín dụng"),
    SALARY(4, "Tài khoản lương"),
    BUSINESS(5, "Tài khoản doanh nghiệp");

    private final int value;

    private final String description;
}
