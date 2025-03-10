package com.system.customer_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KycRequest implements Serializable {

    String customerId;
    String phone;
    String mail;
    String gender;
    LocalDate dob;
    String placeOrigin;
    String identityNumber;
    byte[] identityCardFront;
    byte[] identityCardBack;
    byte[] avatar;

}
