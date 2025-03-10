package com.system.common_library.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportType {
    ACCOUNT("Báo cáo tài khoản"),
    TRANSACTION("Báo cáo giao dịch"),
    LOAN("Báo cáo khoản vay");

    private final String description;
}
