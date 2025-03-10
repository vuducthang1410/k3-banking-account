package com.system.common_library.dto.request.transaction;

import com.system.common_library.enums.TransactionType;
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
public class CoreTransactionRollbackDTO implements Serializable {

    // Internal transaction
    @NotNull(message = "{" + Constant.ACCOUNT_NUMBER_REQUIRE + "}", groups = InternalValidationGroup.class)
    @Size(min = 2, max = 255, message = "{" + Constant.ACCOUNT_NUMBER_SIZE + "}", groups = InternalValidationGroup.class)
    private String senderAccountNumber;

    @Range(min = -1000000000, max = -1, message = "{" + Constant.INVALID_AMOUNT_NEGATE + "}", groups = InternalValidationGroup.class)
    private BigDecimal senderAmount;

    @NotNull(message = "{" + Constant.ACCOUNT_NUMBER_REQUIRE + "}", groups = InternalValidationGroup.class)
    @Size(min = 2, max = 255, message = "{" + Constant.ACCOUNT_NUMBER_SIZE + "}", groups = InternalValidationGroup.class)
    private String receiverAccountNumber;

    @Range(min = 1, max = 1000000000, message = "{" + Constant.INVALID_AMOUNT + "}", groups = InternalValidationGroup.class)
    private BigDecimal receiverAmount;

    // External/System transaction
    @NotNull(message = "{" + Constant.ACCOUNT_NUMBER_REQUIRE + "}",
            groups = {ExternalValidationGroup.class, SystemValidationGroup.class})
    @Size(min = 2, max = 255, message = "{" + Constant.ACCOUNT_NUMBER_SIZE + "}",
            groups = {ExternalValidationGroup.class, SystemValidationGroup.class})
    private String customerAccountNumber;

    @Range(min = -1000000000, max = 1000000000, message = "{" + Constant.INVALID_AMOUNT + "}",
            groups = {ExternalValidationGroup.class, SystemValidationGroup.class})
    private BigDecimal customerAmount;

    // Transaction
    @NotNull(message = "{" + Constant.ACCOUNT_NUMBER_REQUIRE + "}",
            groups = SystemValidationGroup.class)
    @Size(min = 2, max = 255, message = "{" + Constant.ACCOUNT_NUMBER_SIZE + "}",
            groups = SystemValidationGroup.class)
    private String masterAccountNumber;

    @Range(min = -1000000000, max = 1000000000, message = "{" + Constant.INVALID_AMOUNT + "}",
            groups = SystemValidationGroup.class)
    private BigDecimal masterAmount;

    @NotNull(message = "{" + Constant.TRANSACTION_TYPE_REQUIRE + "}")
    private TransactionType type;

    @NotNull(message = "{" + Constant.REFERENCE_CODE_REQUIRE + "}")
    private String referenceCode;

    public interface InternalValidationGroup {
    }

    public interface ExternalValidationGroup {
    }

    public interface SystemValidationGroup {
    }
}
