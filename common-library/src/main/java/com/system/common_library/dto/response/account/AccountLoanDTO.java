package com.system.common_library.dto.response.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountLoanDTO implements Serializable {
    private String loanAccountId;
}
