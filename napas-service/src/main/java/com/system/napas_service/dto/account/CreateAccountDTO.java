package com.system.napas_service.dto.account;

import com.system.napas_service.util.Constant;
import com.system.napas_service.validation.annotation.AccountIdConstraint;
import com.system.napas_service.validation.annotation.AccountNumberConstraint;
import com.system.napas_service.validation.annotation.AvailableBalanceConstraint;
import com.system.napas_service.validation.annotation.BankConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AvailableBalanceConstraint
public class CreateAccountDTO implements Serializable {

    @BankConstraint
    @NotNull(message = "{" + Constant.BANK_ID_REQUIRE + "}")
    private String bankId;

    @AccountIdConstraint
    @NotNull(message = "{" + Constant.ACCOUNT_ID_REQUIRE + "}")
    @Size(min = 2, max = 255, message = "{" + Constant.ACCOUNT_ID_SIZE + "}")
    private String accountId;

    @AccountNumberConstraint
    @NotNull(message = "{" + Constant.ACCOUNT_NUMBER_REQUIRE + "}")
    @Size(min = 2, max = 255, message = "{" + Constant.ACCOUNT_NUMBER_SIZE + "}")
    private String accountNumber;

    @Range(min = 0, max = 1000000000, message = "{" + Constant.INVALID_BALANCE + "}")
    private BigDecimal balance;

    @Range(min = 0, max = 1000000000, message = "{" + Constant.INVALID_AVAILABLE_BALANCE + "}")
    private BigDecimal availableBalance;

    private String customerName;

    @NotNull(message = "{" + Constant.IS_ACTIVE_REQUIRE + "}")
    private Boolean isActive;

    private String description;
}
