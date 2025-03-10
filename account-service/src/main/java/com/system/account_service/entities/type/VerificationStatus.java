package com.system.account_service.entities.type;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum VerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED;

    @JsonCreator
    public static VerificationStatus fromString(String value) {
        if (value == null) {
            return null;
        }

        try {
            return VerificationStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + value);
        }
    }
}
