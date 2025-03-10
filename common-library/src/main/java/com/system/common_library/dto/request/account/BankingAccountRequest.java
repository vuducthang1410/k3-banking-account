package com.system.common_library.dto.request.account;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankingAccountRequest implements Serializable {
    private String customerId;
    private BigDecimal balance;
    private String status;
    private String nickname;
}