package com.system.common_library.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Initiator {
    CUSTOMER("Khách hàng"),
    EMPLOYEE("Nhân viên"),
    SYSTEM("Hệ thống"),
    THIRD_PARTY("Bên thứ ba");

    private final String description;
}
