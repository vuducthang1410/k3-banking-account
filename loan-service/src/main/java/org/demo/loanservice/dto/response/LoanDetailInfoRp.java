package org.demo.loanservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanDetailInfoRp {
    private String createdDate;
    private String dateDisbursement;
    private String douDate;
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

    @Schema(description = "The unit of loan term", example = "Months")
    private String unit;
    private String requestStatus;
    private String loanStatus;
}
