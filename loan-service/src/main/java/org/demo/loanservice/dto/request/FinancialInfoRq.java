package org.demo.loanservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.demo.loanservice.validatedCustom.interfaceValidate.UnitValidation;

import java.math.BigDecimal;

@Data
@Schema(description = "Request object for financial information")
public class FinancialInfoRq {

    @UnitValidation
    @Schema(description = "Unit of financial information", example = "USD")
    private String unit;

    @Min(0)
    @Schema(description = "Income amount", example = "50000")
    private BigDecimal income;

    @NotBlank
    @Schema(description = "Source of income", example = "Salary")
    private String incomeSource;

    @NotBlank
    @Schema(description = "Type of income", example = "Fixed")
    private String incomeType;
}
