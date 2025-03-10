package com.system.common_library.dto.notifcation;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailNotiDTO implements Serializable {
    @NotNull
    private String customerCIF;
    @NotNull
    private String fullName;
    @NotNull
    private String email;
    @NotNull
    private String phoneNumber;
}
