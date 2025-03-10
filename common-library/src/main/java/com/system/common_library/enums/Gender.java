package com.system.common_library.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Gender {
    FEMALE("Nữ"),
    MALE("Nam");

    private final String description;
}
