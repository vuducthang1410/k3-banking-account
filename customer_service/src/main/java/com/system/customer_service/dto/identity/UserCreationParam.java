package com.system.customer_service.dto.identity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationParam implements Serializable {
    String username;
    boolean enabled;
    String email;
    boolean emailVerified;
    String firstName;
    String lastName;
    List<Credential> credentials;
}
