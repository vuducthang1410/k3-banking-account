package com.system.common_library.dto.response.account;

import com.system.common_library.enums.ObjectStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccountInfoDTO implements Serializable {
    @NonNull
    @NotBlank
    private String accountLoanId;
    @NonNull
    @NotBlank
    private ObjectStatus statusLoanAccount;
    @NonNull
    @NotBlank
    private String loanBalance;
    @NonNull
    @NotBlank
    private String loanAccountNumber;
}
