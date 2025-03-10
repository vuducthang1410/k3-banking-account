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
public class CreateInternalTransactionDTO implements Serializable {

    @NotNull(message = "{" + Constant.SENDER_ACCOUNT_REQUIRE + "}")
    @Size(min = 2, max = 255, message = "{" + Constant.SENDER_ACCOUNT_SIZE + "}")
    private String senderAccountNumber;

    @Range(min = -1000000000, max = -1, message = "{" + Constant.INVALID_AMOUNT_NEGATE + "}")
    private BigDecimal senderAmount;

    @NotNull(message = "{" + Constant.RECEIVER_ACCOUNT_REQUIRE + "}")
    @Size(min = 2, max = 255, message = "{" + Constant.RECEIVER_ACCOUNT_SIZE + "}")
    private String receiverAccountNumber;

    @Range(min = 1, max = 1000000000, message = "{" + Constant.INVALID_AMOUNT + "}")
    private BigDecimal receiverAmount;

    @NotNull(message = "{" + Constant.ACCOUNT_NUMBER_REQUIRE + "}")
    @Size(min = 2, max = 255, message = "{" + Constant.ACCOUNT_NUMBER_SIZE + "}")
    private String masterAccountNumber;

    @Range(min = 0, max = 1000000000, message = "{" + Constant.INVALID_FEE + "}")
    private BigDecimal fee;

    private String note;

    private String description;
}
