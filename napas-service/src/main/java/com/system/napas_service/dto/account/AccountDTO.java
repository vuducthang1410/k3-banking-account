package com.system.napas_service.dto.account;

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
public class AccountDTO implements Serializable {

    private String id;
    private String bankId;
    private String bankName;
    private String accountId;
    private String accountNumber;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private String customerName;
    private Boolean isActive;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private String description;
    private Boolean state;
    private Boolean status;
}
