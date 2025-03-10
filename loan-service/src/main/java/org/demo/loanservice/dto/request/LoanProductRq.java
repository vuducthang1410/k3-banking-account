package org.demo.loanservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;

@Data
public class LoanProductRq {
    @NotBlank
    private String nameLoanProduct;
    @Min(0)
    private BigDecimal loanLimit;
    @NotBlank
    @NonNull
    private String description;
    @NotBlank
    @NonNull
    private String utilities;
    @NotBlank
    @NonNull
    private String loanCondition;
    @NotBlank
    private String loanForm;
    @NotBlank
    private String applicableObjects;
    @Min(0)
    @Max(48)
    private Integer termLimit;
}

