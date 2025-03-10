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
public class AccountCoreDTO implements Serializable {

    private String id;
    private String account;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private AccountType accountType;
    private Boolean isActive;
    private String customerId;
    private String customerCifCode;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private String description;
    private Boolean state;
    private Boolean status;
}
