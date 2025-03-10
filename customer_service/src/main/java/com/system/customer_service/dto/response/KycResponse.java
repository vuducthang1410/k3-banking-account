package com.system.customer_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KycResponse implements Serializable {

    String id;
    String customerId;
    String status;
    String phone;
    String identityNumber;
    String identityCardFront;
    String identityCardBack;
    String avatar;
}
