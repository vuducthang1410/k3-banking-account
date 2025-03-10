package com.system.common_library.dto.request.account;

import com.system.common_library.util.Constant;
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
public class UpdateAccountCoreDTO implements Serializable {

    @NotNull(message = "{" + Constant.CURRENCY_REQUIRE + "}")
    @Size(min = 2, max = 255, message = "{" + Constant.CURRENCY_SIZE + "}")
    private String currency;
    
    @NotNull(message = "{" + Constant.IS_ACTIVE_REQUIRE + "}")
    private Boolean isActive;

    private String description;
}
