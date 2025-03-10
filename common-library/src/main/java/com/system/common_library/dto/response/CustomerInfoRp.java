package com.system.common_library.dto.response;

import com.system.common_library.enums.ObjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfoRp implements Serializable {
    private String customerId;
    private String customerNumber;
    private String fullName;
    private String cccd;
    private Date dateOfBirth;
    private String phoneNumber;
    private ObjectStatus customerStatus;
}
