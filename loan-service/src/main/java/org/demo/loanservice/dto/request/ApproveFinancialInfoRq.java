package org.demo.loanservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NonNull;
import org.demo.loanservice.validatedCustom.interfaceValidate.RequestStatusValidation;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Request object for approving financial information")
public class ApproveFinancialInfoRq {

    @NonNull
    @Schema(description = "Unique identifier of the financial information", example = "fin-123456")
    private String financialInfoId;

    @RequestStatusValidation
    @Schema(description = "Status of the financial information", example = "APPROVED")
    private String statusFinancialInfo;

    @Schema(description = "Additional notes or comments", example = "Loan approved based on financial stability")
    private String note;

    @Min(0)
    @Max(1000000000)
    @Schema(description = "Maximum loan amount limit", example = "500000")
    private BigDecimal loanAmountLimit;
}
