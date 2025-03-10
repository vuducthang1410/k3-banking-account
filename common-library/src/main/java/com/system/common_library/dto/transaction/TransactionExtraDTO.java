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
public class TransactionExtraDTO implements Serializable {

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
    private String swiftCode;

    // Internal transaction
    private String type;
    private String typeName;

    // Transaction
    private String transactionCode;
    private String senderAccountId;
    private String senderAccount;
    private String senderAccountType;
    private String senderAccountTypeName;
    private String senderAccountName;
    private String receiverAccountId;
    private String receiverAccount;
    private String receiverAccountType;
    private String receiverAccountTypeName;
    private String receiverAccountName;
    private String referenceCode;
    private String feePayer;
    private String feePayerName;
    private String note;
    private String otpCode;
    private String initiator;
    private String initiatorName;
    private String transactionType;
    private String transactionTypeName;
    private String method;
    private String methodName;
    private String creatorId;

    // Transaction state
    private String transactionState;
    private String transactionStateName;

    // Transaction detail
    private String customerId;
    private String account;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal netAmount;
    private BigDecimal previousBalance;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;
    private String direction;
    private String directionName;
}
