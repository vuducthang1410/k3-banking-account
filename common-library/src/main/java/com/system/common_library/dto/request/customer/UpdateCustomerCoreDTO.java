package com.system.common_library.dto.request.customer;

import com.system.common_library.util.Constant;
import jakarta.validation.constraints.Email;
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
public class UpdateCustomerCoreDTO implements Serializable {

    @Email(regexp = "^(.+)@(\\S+)$", message = "{" + Constant.INVALID_FORMAT_EMAIL + "}")
    private String email;

    private String address;

    @NotNull(message = "{" + Constant.IS_ACTIVE_REQUIRE + "}")
    private Boolean isActive;

    private String description;
}
