package com.system.common_library.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeePayer {
    SENDER("Người chuyển"),
    RECEIVER("Người nhận");

    private final String description;
}
