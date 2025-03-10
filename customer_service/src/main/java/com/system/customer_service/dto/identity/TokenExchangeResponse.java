package com.system.customer_service.dto.identity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TokenExchangeResponse implements Serializable {
    String accessToken;
    String expiresIn;
    String refreshExpiresIn;
    String refreshToken;
    String tokenType;
    String idToken;
    String scope;
}
