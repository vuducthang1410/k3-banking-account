package com.system.account_service.exception.payload;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ExistedDataException extends RuntimeException {
    private String msgKey;

    public ExistedDataException(String msgKey) {
        this.msgKey = msgKey;
    }
}
