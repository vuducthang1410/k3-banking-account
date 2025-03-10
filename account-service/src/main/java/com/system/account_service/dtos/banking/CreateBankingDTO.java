package com.system.account_service.dtos.banking;

import com.system.account_service.dtos.account_common.CreateAccountCommonDTO;
import com.system.account_service.utils.MessageKeys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBankingDTO implements Serializable {
    @Valid
    private CreateAccountCommonDTO accountCommon;

    @NotBlank(message = MessageKeys.MESSAGES_BLANK_NICK_NAME)
    private String nickName;

    @NotBlank(message = MessageKeys.MESSAGES_BLANK_BRANCH_ID)
    private String branchId;
}
