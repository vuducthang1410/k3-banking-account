package com.system.common_library.dto.transaction;

import com.system.common_library.enums.AccountType;
import com.system.common_library.util.Constant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSystemDTO implements Serializable {

    @NotNull(message = "{" + Constant.CIF_CODE_REQUIRE + "}")
    @Size(min = 8, max = 11, message = "{" + Constant.CIF_CODE_SIZE + "}")
    private String cifCode;

    @Email(regexp = "^(.+)@(\\S+)$", message = "{" + Constant.INVALID_FORMAT_EMAIL + "}")
    private String email;

    private String customerAccountId;

    @NotNull(message = "{" + Constant.CUSTOMER_ACCOUNT_REQUIRE + "}")
    @Size(min = 2, max = 255, message = "{" + Constant.CUSTOMER_ACCOUNT_SIZE + "}")
    private String customerAccount;

    private AccountType customerAccountType;

    @NotNull(message = "{" + Constant.CUSTOMER_NAME_REQUIRE + "}")
    @Size(min = 2, max = 255, message = "{" + Constant.CUSTOMER_NAME_SIZE + "}")
    private String customerAccountName;

    @Range(min = -1000000000, max = 1000000000, message = "{" + Constant.INVALID_AMOUNT + "}")
    private BigDecimal amount;

    private String note;

    private String description;
}
