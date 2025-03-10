package org.demo.loanservice.controllers.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ServerErrorException extends RuntimeException {
    private String transactionId;
    private String code;

    public ServerErrorException(String transactionId, String code) {
        super();
        this.transactionId = transactionId;
        this.code = code;
    }

    public ServerErrorException() {
        super();
    }

}
