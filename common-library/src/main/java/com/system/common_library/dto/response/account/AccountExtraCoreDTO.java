package com.system.common_library.dto.response.account;

import com.system.common_library.enums.AccountType;
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
public class AccountExtraCoreDTO implements Serializable {

    private String id;
    private String accountId;
    private String accountNumber;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenditure;
    private String customerId;
    private String customerCifCode;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String currency;
    private Boolean isActive;
    private LocalDateTime lastTransactionDate;
    private AccountType accountType;
    private String accountTypeName;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private String description;
    private Boolean state;
    private Boolean status;
}
