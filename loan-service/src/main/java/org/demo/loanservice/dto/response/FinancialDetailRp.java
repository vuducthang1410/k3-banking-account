package org.demo.loanservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinancialDetailRp {
    private String customerId;
    private String customerName;
    private String numberPhone;
    private String identificationNumber;
    private String dateOfBirth;
    private String financialInfoId;
    private String amountLoanLimit;
    private String amountMaybeLoanRemain;
    private String requestStatus;
    private String balanceBankingAccount;
    private String bankingAccountNumber;
}
