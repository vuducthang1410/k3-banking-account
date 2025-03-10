package com.system.customer_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordRequest implements Serializable {
    @NotBlank(message = "VALIDATION_EMAIL_NOT_BLANK")
    @Email(message = "VALIDATION_EMAIL_INVALID")
    private String mail;

    @NotNull(message = "PASSWORD_MANDATORY")
    @NotEmpty(message = "PASSWORD_MANDATORY")
    @NotBlank(message = "PASSWORD_MANDATORY")
    @Size(min = 6, message = "INVALID_PASSWORD")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$", message = "VALIDATION_PASSWORD_PATTERN")
    private String newPassword;

    @NotNull(message = "PASSWORD_MANDATORY")
    @NotEmpty(message = "PASSWORD_MANDATORY")
    @NotBlank(message = "PASSWORD_MANDATORY")
    @Size(min = 6, message = "INVALID_PASSWORD")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$", message = "VALIDATION_PASSWORD_PATTERN")
    private String confirmationPassword;

    private Integer otp;
}
