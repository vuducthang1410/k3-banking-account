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
public class IntrospectRequest implements Serializable {

    @NotNull(message = "{token.mandatory}")
    @NotEmpty(message = "{token.mandatory}")
    @NotBlank(message = "{token.mandatory}")
    String refreshToken;
}
