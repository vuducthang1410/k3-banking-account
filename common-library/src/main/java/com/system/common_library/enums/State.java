package com.system.common_library.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum State {
    PENDING("Chờ duyệt"),
    COMPLETED("Thành công"),
    FAILED("Thất bại");

    private final String description;
}
