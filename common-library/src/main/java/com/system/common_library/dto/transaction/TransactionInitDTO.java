package com.system.common_library.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionInitDTO implements Serializable {

    // Base entity
    private String id;
    private LocalDateTime dateCreated;
    private String description;
    private Boolean status;

    // External bank
    private String externalBankName;
    private String externalBankShortName;
    private String externalBankCode;
    private String externalBankLogo;

    // External transaction
    private String napasCode;

    // Internal transaction
    private String type;

    // Transaction
    private String transactionCode;
    private String senderAccountId;
    private String senderAccount;
    private String senderAccountType;
    private String senderAccountName;
    private String receiverAccountId;
    private String receiverAccount;
    private String receiverAccountType;
    private String receiverAccountName;
    private String referenceCode;
    private String feePayer;
    private String note;
    private String initiator;
    private String transactionType;
    private String method;

    // Transaction state
    private String transactionState;

    // Transaction detail
    private BigDecimal amount;
    private BigDecimal fee;
}
