package com.system.customer_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest implements Serializable {

    @NotNull(message = "{phone.mandatory}")
    @NotEmpty(message = "{phone.mandatory}")
    @NotBlank(message = "{phone.mandatory}")
    String phone;

    @NotNull(message = "{password.mandatory}")
    @NotEmpty(message = "{password.mandatory}")
    @NotBlank(message = "{password.mandatory}")
    String password;
}
