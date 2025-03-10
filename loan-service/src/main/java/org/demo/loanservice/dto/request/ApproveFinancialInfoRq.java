package org.demo.loanservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NonNull;
import org.demo.loanservice.validatedCustom.interfaceValidate.RequestStatusValidation;

import java.math.BigDecimal;

@Data
public class ApproveFinancialInfoRq {
    @NonNull
    private String financialInfoId;
    @RequestStatusValidation
    private String statusFinancialInfo;
    private String note;
    @Min(0)
    @Max(1000000000)
    private BigDecimal loanAmountLimit;
}
