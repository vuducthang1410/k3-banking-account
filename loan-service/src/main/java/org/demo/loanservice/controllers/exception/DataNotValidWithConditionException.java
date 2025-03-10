package org.demo.loanservice.controllers.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.loanservice.common.MessageData;

@EqualsAndHashCode(callSuper = true)
@Data
public class DataNotValidWithConditionException extends RuntimeException {
    private final String messageKey;
    private final String code;
    private final String condition;

    public DataNotValidWithConditionException(MessageData messageData, String condition) {
        super();
        this.messageKey = messageData.getKeyMessage();
        this.code = messageData.getCode();
        this.condition = condition;
    }
}
