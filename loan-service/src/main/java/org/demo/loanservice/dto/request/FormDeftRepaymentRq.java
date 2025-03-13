package org.demo.loanservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.demo.loanservice.common.MessageValue;
import org.demo.loanservice.validatedCustom.interfaceValidate.FormDeftRepaymentValidation;


import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Request object for defining a repayment form")
public class FormDeftRepaymentRq {

    @NotBlank(message = MessageValue.VALID_DTO_NAME_NOT_BLANK)
    @Schema(description = "Name of the repayment form", example = "Monthly Installment")
    private String formName;

    @NotBlank(message = MessageValue.VALID_DTO_DESCRIPTION_NOT_BLANK)
    @Schema(description = "Description of the repayment form", example = "A repayment form for monthly installments")
    private String description;

    @FormDeftRepaymentValidation
    @Schema(description = "Unique code for the repayment form", example = "FORM_12345")
    private String code;
}

