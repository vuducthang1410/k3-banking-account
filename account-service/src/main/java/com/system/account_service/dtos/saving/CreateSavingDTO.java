package com.system.account_service.dtos.saving;

import com.system.account_service.utils.MessageKeys;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateSavingDTO {
    @NotBlank(message = MessageKeys.MESSAGES_BLANK_BANKING_ACCOUNT)
    private String cifCode;

//    @NotBlank(message = MessageKeys.MESSAGES_BLANK_BRANCH_ID)
    private String branchId;

    @DecimalMin(value = "100000.00", message = MessageKeys.MESSAGES_SCOPE_SAVING_MIN_BALANCE)
    private BigDecimal balance;

    private Integer term;
}
