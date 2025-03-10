package com.system.customer_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerUpdateRequest implements Serializable {
    String address;
    String mail;
    String firstName;
    String lastName;

}
