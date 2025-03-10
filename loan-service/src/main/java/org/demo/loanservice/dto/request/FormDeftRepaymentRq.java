package org.demo.loanservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.demo.loanservice.common.MessageValue;
import org.demo.loanservice.validatedCustom.interfaceValidate.FormDeftRepaymentValidation;


@Data
public class FormDeftRepaymentRq {
    @NotBlank(message = MessageValue.VALID_DTO_NAME_NOT_BLANK)
    private String formName;
    @NotBlank(message = MessageValue.VALID_DTO_DESCRIPTION_NOT_BLANK)
    private String description;
    @FormDeftRepaymentValidation
    private String code;
}
