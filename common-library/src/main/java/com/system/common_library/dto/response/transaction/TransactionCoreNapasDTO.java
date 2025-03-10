package com.system.common_library.dto.response.transaction;

import com.system.common_library.enums.Direction;
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
public class TransactionCoreNapasDTO implements Serializable {

    // Base entity
    private String id;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private String description;
    private Boolean state;
    private Boolean status;

    // Account
    private String accountId;
    private String accountNumber;
    private String customerId;
    private String customerName;
    private String customerPhone;

    // Transaction
    private BigDecimal amount;
    private BigDecimal previousBalance;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;
    private String referenceCode;
    private Direction direction;
    private String note;
}
