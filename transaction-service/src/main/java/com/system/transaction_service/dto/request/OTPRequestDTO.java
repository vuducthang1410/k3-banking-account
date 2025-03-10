package com.system.transaction_service.dto.request;

import com.system.transaction_service.util.Constant;
import com.system.transaction_service.validation.annotation.TransactionConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPRequestDTO implements Serializable {

    @Email(regexp = "^(.+)@(\\S+)$", message = "{" + Constant.INVALID_FORMAT_EMAIL + "}")
    private String email;

    @TransactionConstraint
    @NotNull(message = "{" + Constant.TRANSACTION_ID_REQUIRE + "}")
    private String transactionId;
}
