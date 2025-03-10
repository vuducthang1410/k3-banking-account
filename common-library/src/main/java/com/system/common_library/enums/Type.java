package com.system.common_library.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Type {
    TRANSFER("Chuyển khoản"),
    DEPOSIT("Nạp tiền"),
    WITHDRAW("Rút tiền");

    private final String description;
}
