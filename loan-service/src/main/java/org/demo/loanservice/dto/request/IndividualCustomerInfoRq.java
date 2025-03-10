package org.demo.loanservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;
import org.demo.loanservice.validatedCustom.interfaceValidate.FormDeftRepaymentValidation;
import org.demo.loanservice.validatedCustom.interfaceValidate.UnitValidation;

import java.math.BigDecimal;

@Data
public class IndividualCustomerInfoRq {
    @NotBlank
    private String loanProductId;
    @FormDeftRepaymentValidation
    private String formDeftRepayment;
    @Min(0)
    private BigDecimal loanAmount;
    @Min(1)
    private Integer loanTerm;
    @UnitValidation
    private String loanUnit;
    @NonNull
    private String cifCode;
}
