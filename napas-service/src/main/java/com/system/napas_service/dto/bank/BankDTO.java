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
public class BankDTO implements Serializable {

    private String id;
    private String name;
    private String shortName;
    private String code;
    private Boolean isAvailable;
    private String logo;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private Boolean state;
    private Boolean status;
}
