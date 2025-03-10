package com.system.common_library.dto.request.account;

import com.system.common_library.enums.AccountType;
import com.system.common_library.util.Constant;
import com.system.common_library.validation.annotation.AccountTypeConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountCoreDTO implements Serializable {

    @NotNull(message = "{" + Constant.CIF_CODE_REQUIRE + "}")
    @Size(min = 8, max = 11, message = "{" + Constant.CIF_CODE_SIZE + "}")
    private String cifCode;

    @NotNull(message = "{" + Constant.CURRENCY_REQUIRE + "}")
    @Size(min = 1, max = 255, message = "{" + Constant.CURRENCY_SIZE + "}")
    private String currency;

    @NotNull(message = "{" + Constant.IS_ACTIVE_REQUIRE + "}")
    private Boolean isActive;

    @AccountTypeConstraint
    private AccountType type;

    private String description;
}
