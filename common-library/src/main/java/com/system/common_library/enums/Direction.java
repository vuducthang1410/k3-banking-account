package com.system.common_library.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Direction {
    SEND("Gửi"),
    RECEIVE("Nhận");

    private final String description;
}
