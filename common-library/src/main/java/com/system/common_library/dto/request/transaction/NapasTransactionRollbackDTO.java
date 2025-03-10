package com.system.common_library.dto.request.transaction;

import com.system.common_library.util.Constant;
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
public class NapasTransactionRollbackDTO implements Serializable {

    @NotNull(message = "{" + Constant.ACCOUNT_NUMBER_REQUIRE + "}")
    @Size(min = 2, max = 255, message = "{" + Constant.ACCOUNT_NUMBER_SIZE + "}")
    private String senderAccount;

    @NotNull(message = "{" + Constant.ACCOUNT_NUMBER_REQUIRE + "}")
    @Size(min = 2, max = 255, message = "{" + Constant.ACCOUNT_NUMBER_SIZE + "}")
    private String receiverAccount;

    @Range(min = 1, max = 1000000000, message = "{" + Constant.INVALID_AMOUNT + "}")
    private BigDecimal amount;

    @NotNull(message = "{" + Constant.TRANSACTION_ID_REQUIRE + "}")
    private String transactionId;
}
