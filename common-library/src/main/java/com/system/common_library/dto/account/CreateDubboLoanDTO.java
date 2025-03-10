package com.system.common_library.dto.account;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDubboLoanDTO implements Serializable {
    @NotBlank(message = "Customer is required!")
    private String customerId;

    @NotBlank(message = "CIF code is required!")
    private String cifCode;

    private String branchId;
}
