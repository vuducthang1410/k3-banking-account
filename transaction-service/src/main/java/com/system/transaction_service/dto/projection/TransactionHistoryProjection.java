package com.system.transaction_service.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionHistoryProjection {

    String getId();

    LocalDateTime getDateCreated();

    BigDecimal getAmount();

    String getDescription();

    Integer getIsTransfer();

    String getSenderAccountNumber();

    String getReceiverAccountNumber();

    String getTransactionType();
}
