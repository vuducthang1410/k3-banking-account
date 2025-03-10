package com.system.account_service.exception.payload;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InvalidParamException extends RuntimeException {
    private String msgKey;

    public InvalidParamException(String msgKey) {
        this.msgKey = msgKey;
    }
}
