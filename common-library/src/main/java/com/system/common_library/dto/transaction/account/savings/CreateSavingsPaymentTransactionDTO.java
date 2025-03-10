package com.system.common_library.dto.transaction.account.savings;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSavingsPaymentTransactionDTO implements Serializable {

    @NotNull(message = "CIF code is required")
    @Size(min = 8, max = 11, message = "The length of cif code is from 8 to 11 characters")
    private String cifCode;

    @NotNull(message = "Account number is required")
    @Size(min = 2, max = 255, message = "The length of account number is from 2 to 255 characters")
    private String paymentAccount;

    @NotNull(message = "Saving account is required")
    @Size(min = 2, max = 255, message = "The length of saving account is from 2 to 255 characters")
    private String savingAccount;

    private String note;

    private String description;
}
