package org.demo.loanservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.demo.loanservice.validatedCustom.interfaceValidate.FormDeftRepaymentValidation;
import org.demo.loanservice.validatedCustom.interfaceValidate.UnitValidation;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Request object for registering an individual customer loan")
public class IndividualCustomerInfoRq {

    @NotBlank
    @Schema(description = "Loan product identifier", example = "LOAN_PROD_001")
    private String loanProductId;

    @FormDeftRepaymentValidation
    @Schema(description = "Repayment form identifier", example = "FORM_DEFT_001")
    private String formDeftRepayment;

    @Min(0)
    @Schema(description = "Requested loan amount", example = "50000")
    private BigDecimal loanAmount;

    @Min(1)
    @Schema(description = "Loan term duration", example = "12")
    private Integer loanTerm;

    @UnitValidation
    @Schema(description = "Loan term unit (e.g., months, years)", example = "MONTHS")
    private String loanUnit;

    @Schema(description = "Customer Identification File (CIF) Code", example = "CIF123456")
    private String cifCode;
}
