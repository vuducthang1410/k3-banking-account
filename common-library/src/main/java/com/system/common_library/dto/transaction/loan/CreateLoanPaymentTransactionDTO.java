package com.system.common_library.dto.transaction.loan;

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
public class CreateLoanPaymentTransactionDTO implements Serializable {

    @NotNull(message = "CIF code is required")
    @Size(min = 8, max = 11, message = "The length of cif code is from 8 to 11 characters")
    private String cifCode;

    @NotNull(message = "Account number is required")
    @Size(min = 2, max = 255, message = "The length of account number is from 2 to 255 characters")
    private String paymentAccount;

    @NotNull(message = "Loan account is required")
    @Size(min = 2, max = 255, message = "The length of loan account is from 2 to 255 characters")
    private String loanAccount;

    @Range(min = 1, max = 10000000000L, message = "Invalid amount (max=10.000.000.000)")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private boolean isCashPayment;

    private String note;

    private String description;
}
