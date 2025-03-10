package com.system.account_service.dtos.credit;

import com.system.account_service.utils.MessageKeys;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCreditDTO {
    @NotBlank(message = MessageKeys.MESSAGES_BLANK_BANKING_ACCOUNT)
    private String cifCode;

//    @NotBlank(message = MessageKeys.MESSAGES_BLANK_BRANCH_ID)
    private String branchId;
}
