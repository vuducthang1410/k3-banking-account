package org.demo.loanservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;
import org.demo.loanservice.validatedCustom.interfaceValidate.RequestStatusValidation;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Request object for approving a loan information")
public class LoanInfoApprovalRq {

    @NonNull
    @NotBlank
    @Schema(description = "Loan detail information ID", example = "LOAN_DETAIL_12345")
    private String loanDetailInfoId;

    @RequestStatusValidation
    @Schema(description = "Loan request status", example = "APPROVED")
    private String requestStatus;

    @Schema(description = "Additional notes or comments", example = "Loan approved based on customer's credit score")
    private String note;
}

