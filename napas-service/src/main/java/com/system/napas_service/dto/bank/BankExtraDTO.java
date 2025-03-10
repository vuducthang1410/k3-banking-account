package com.system.napas_service.dto.bank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankExtraDTO implements Serializable {

    private String id;
    private String name;
    private String shortName;
    private String code;
    private String napasCode;
    private String swiftCode;
    private String contactInfo;
    private Boolean isAvailable;
    private String logo;
    private String logoImageName;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private String description;
    private Boolean state;
    private Boolean status;
}
