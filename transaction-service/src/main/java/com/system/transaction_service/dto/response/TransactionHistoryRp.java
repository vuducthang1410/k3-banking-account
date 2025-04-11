package com.system.transaction_service.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class TransactionHistoryRp {
    private String id;
    private LocalDateTime dateCreated;
    private BigDecimal amount;
    private String description;
    private Integer isTransfer;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private String transactionType;
}