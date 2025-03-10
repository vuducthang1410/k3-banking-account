package org.demo.loanservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.demo.loanservice.validatedCustom.interfaceValidate.UnitValidation;

import java.math.BigDecimal;

@Data
public class FinancialInfoRq {
    @UnitValidation
    private String unit;
    @Min(0)
    private BigDecimal income;
    @NotBlank
    private String incomeSource;
    @NotBlank
    private String incomeType;
    @NotBlank
    private String cifCode;
}
