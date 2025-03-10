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
public class TransactionDTO implements Serializable {

    // Base entity
    private String id;
    private LocalDateTime dateCreated;
    private String description;
    private Boolean status;

    // Transaction
    private String transactionCode;
    private String referenceCode;
    private String note;
    private String transactionType;
    private String transactionTypeName;

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
