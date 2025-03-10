package com.system.customer_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerResponse implements Serializable {
    String id;
    String userId;
    String cifCode;
    String accountNumber;
    String phone;
    String address;
    LocalDate dob;
    String mail;
    String firstName;
    String lastName;
    String identityCard;
    String gender;
    String avatar;
    String status;

}
