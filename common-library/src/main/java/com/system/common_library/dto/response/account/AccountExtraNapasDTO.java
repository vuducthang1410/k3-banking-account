package com.system.common_library.dto.response.account;

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
public class AccountExtraNapasDTO implements Serializable {

    private String id;
    private String bankId;
    private String bankName;
    private String bankCode;
    private String bankNapasCode;
    private String bankSwiftCode;
    private String bankLogo;
    private String accountId;
    private String accountNumber;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenditure;
    private String customerName;
    private Boolean isActive;
    private LocalDateTime lastTransactionDate;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private String description;
    private Boolean state;
    private Boolean status;
}
