package com.system.common_library.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {
    INTERNAL("Nội bộ"),
    EXTERNAL("Bên ngoài"),
    PAYMENT("Thanh toán"),
    SYSTEM("Hệ thống"),
    ROLLBACK("Quay lại");

    private final String description;
}
