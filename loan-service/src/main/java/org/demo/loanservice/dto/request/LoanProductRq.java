package org.demo.loanservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Request object for loan product details")
public class LoanProductRq {

    @NotBlank
    @Schema(description = "Name of the loan product", example = "Personal Loan")
    private String nameLoanProduct;

    @Min(0)
    @Schema(description = "Maximum loan limit for the product", example = "50000000")
    private BigDecimal loanLimit;

    @NotBlank
    @NonNull
    @Schema(description = "Description of the loan product", example = "A short-term personal loan with flexible repayment options.")
    private String description;

    @NotBlank
    @NonNull
    @Schema(description = "Utilities and benefits of the loan", example = "Low interest rates, no hidden fees")
    private String utilities;

    @NotBlank
    @NonNull
    @Schema(description = "Conditions required for loan approval", example = "Minimum salary of 10,000,000 VND per month")
    private String loanCondition;

    @NotBlank
    @Schema(description = "Form of the loan", example = "Secured Loan")
    private String loanForm;

    @NotBlank
    @Schema(description = "Applicable objects for this loan", example = "Salaried employees, self-employed individuals")
    private String applicableObjects;

    @Min(0)
    @Max(48)
    @Schema(description = "Maximum loan term limit in months", example = "36")
    private Integer termLimit;
}

