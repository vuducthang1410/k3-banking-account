package com.system.customer_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerWorkflowRequest implements Serializable {
    private String phone;
    private String password;
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private String address;
    private String placeOrigin;
    private String identityCard;
    private LocalDate dob;
    private byte[] identityCardFront;
    private byte[] identityCardBack;
    private byte[] avatar;
}
