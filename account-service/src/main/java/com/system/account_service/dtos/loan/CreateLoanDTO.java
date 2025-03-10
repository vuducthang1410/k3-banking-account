package com.system.account_service.dtos.loan;

import com.system.account_service.dtos.account_common.CreateAccountCommonDTO;
import com.system.account_service.utils.MessageKeys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLoanDTO {
    @Valid
    private CreateAccountCommonDTO accountCommon;

    @NotBlank(message = MessageKeys.MESSAGES_BLANK_BANKING_ACCOUNT)
    private String bankingAccountId;

    private String branchId;

    private BigDecimal balance;
}
