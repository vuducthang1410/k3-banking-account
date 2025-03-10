package org.demo.loanservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Detailed information about a customer's loan.")
public class CustomerLoanDetailInfoRp {

    @Schema(description = "The customer's unique identifier", example = "")
    private String customerId;

    @Schema(description = "The customer's full name", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "The method of debt repayment", example = "Monthly Installments")
    private String formDeftRepayment;

    @Schema(description = "The customer's identity card number", example = "015203002555")
    private String identityCard;

    @Schema(description = "The interest rate of the loan", example = "5.5")
    private Double interestRate;

    @Schema(description = "The total loan amount", example = "5000000")
    private String loanAmount;

    @Schema(description = "The unique identifier for loan details", example = "")
    private String loanDetailInfoId;

    @Schema(description = "The name of the loan product", example = "Home Loan")
    private String loanProductName;

    @Schema(description = "The duration of the loan term", example = "36")
    private Integer loanTerm;

    @Schema(description = "The customer's phone number", example = "+84 346548120")
    private String phone;

    @Schema(description = "The unit of loan term", example = "Months")
    private String unit;

    private String createdTime;
    private String loanStatus;
}

