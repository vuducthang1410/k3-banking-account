package com.system.customer_service.dto.identity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticateParam implements Serializable {
    String grant_type;
    String client_id;
    String client_secret;
    String username;
    String password;
    String scope;
}
