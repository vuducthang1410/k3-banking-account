package org.demo.loanservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;
import org.demo.loanservice.validatedCustom.interfaceValidate.RequestStatusValidation;

@Data
public class LoanInfoApprovalRq {
    @NonNull
    @NotBlank
    private String loanDetailInfoId;
    @RequestStatusValidation
    private String requestStatus;
    private String note;
}
