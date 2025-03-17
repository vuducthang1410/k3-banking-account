package org.demo.loanservice.dto.enumDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoanCategory {
    UNPAID_REPAYMENT("Khoản chưa trả"),
    PAID_REPAYMENT("Khoản đã trả"),
    PENDING_LOAN("Khoản đang chờ");

    private final String label;
}