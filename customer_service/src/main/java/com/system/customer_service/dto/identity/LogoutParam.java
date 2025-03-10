package com.system.customer_service.dto.identity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LogoutParam implements Serializable {
    String client_id;
    String client_secret;
    String refresh_token;
}
