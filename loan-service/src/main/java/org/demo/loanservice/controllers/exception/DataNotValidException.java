package org.demo.loanservice.controllers.exception;

import lombok.Getter;
import org.demo.loanservice.common.MessageData;

@Getter
public class DataNotValidException extends RuntimeException {
    private final String messageKey;
    private final String code;

    public DataNotValidException(MessageData messageData) {
        super();
        this.messageKey = messageData.getKeyMessage();
        this.code = messageData.getCode();
    }
}
