package com.system.common_library.dto.response.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCoreDTO implements Serializable {

    private String id;
    private String cifCode;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private LocalDate birthday;
    private Boolean isActive;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private String description;
    private Boolean state;
    private Boolean status;
}
