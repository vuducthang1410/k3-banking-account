package com.example.reporting_service.model.dto;

import com.system.common_library.enums.AccountType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalAccountRequest {
    private String account;
    private AccountType accountType;
    private String customerId;

    private LocalDateTime startTransactionDate = LocalDateTime.now().minusMonths(1);
    private LocalDateTime endTransactionDate = LocalDateTime.now();

}
