package org.demo.loanservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanProductForUserRp {
    private String loanProductId;
    private String nameLoanProduct;
    private String urlImage;
    private double minInterestRate;
    private double maxInterestRate;
    private String maxLoanAmount;
    private int maxLoanTerm;
}