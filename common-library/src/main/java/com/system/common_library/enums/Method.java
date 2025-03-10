package com.system.common_library.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Method {
    ONLINE_BANKING("Ngân hàng trực tuyến"),
    IN_BRANCH("Tại chi nhánh"),
    SYSTEM("Hệ thống tự động");

    private final String description;
}
