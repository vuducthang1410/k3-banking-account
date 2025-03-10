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
public class CreateDubboBankingDTO implements Serializable {

    @NotBlank(message = "Customer is required!")
    private String customerId;

    @NotBlank(message = "CIF code is required!")
    private String cifCode;

    @NotBlank(message = "Full name is required!")
    private String fullName;

    @NotBlank(message = "Phone is required!")
    private String phone;

    @NotBlank(message = "Email is required!")
    private String email;

    @NotBlank(message = "Branch banking is required!")
    private String branchId;
}
