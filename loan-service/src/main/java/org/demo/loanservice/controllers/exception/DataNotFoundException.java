package org.demo.loanservice.controllers.exception;

import lombok.Getter;
import org.demo.loanservice.common.MessageData;

@Getter
public class DataNotFoundException extends RuntimeException{
    private final String messageKey;
    private final String code;
    public DataNotFoundException(MessageData messageData){
        super();
        this.messageKey = messageData.getKeyMessage();
        this.code = messageData.getCode();
    }
}
