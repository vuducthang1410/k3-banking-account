package com.system.common_library.dto.response.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceDTO implements Serializable {
    private String accountNumber;
    private BigDecimal balance;
}
