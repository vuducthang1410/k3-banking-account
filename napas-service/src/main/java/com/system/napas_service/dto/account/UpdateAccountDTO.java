package com.system.napas_service.dto.account;

import com.system.napas_service.util.Constant;
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
public class UpdateAccountDTO implements Serializable {

    private String customerName;

    @NotNull(message = "{" + Constant.IS_ACTIVE_REQUIRE + "}")
    private Boolean isActive;

    private String description;
}
