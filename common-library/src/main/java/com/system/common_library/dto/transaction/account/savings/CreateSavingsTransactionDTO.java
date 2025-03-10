package com.system.common_library.dto.transaction.account.savings;

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
public class CreateSavingsTransactionDTO implements Serializable {

    @NotNull(message = "CIF code is required")
    @Size(min = 8, max = 11, message = "The length of cif code is from 8 to 11 characters")
    private String cifCode;

    @NotNull(message = "Saving account is required")
    @Size(min = 2, max = 255, message = "The length of saving account is from 2 to 255 characters")
    private String savingAccount;

    @Range(min = 1, max = 10000000000L, message = "Invalid amount (max=10.000.000.000)")
    private BigDecimal amount;

    private String note;

    private String description;
}
