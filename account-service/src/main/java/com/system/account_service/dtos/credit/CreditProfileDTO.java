package com.system.account_service.dtos.credit;

import com.system.account_service.entities.CreditAccount;
import com.system.account_service.utils.MessageKeys;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditProfileDTO {
//    @NotBlank(message = MessageKeys.MESSAGES_BLANK_CUSTOMER_ID)
//    private String customerId;

    private String company;

    @NotBlank(message = MessageKeys.MESSAGED_BLANK_PHONE)
    private String phone;

    @NotNull(message = MessageKeys.MESSAGED_BLANK_INCOME)
    @DecimalMin(value = "100000.0", message = MessageKeys.MESSAGES_SCOPE_MIN_INCOME)
    private BigDecimal income;

    private CreditAccount creditAccount;
}
