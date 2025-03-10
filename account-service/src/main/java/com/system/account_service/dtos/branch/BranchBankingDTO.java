package com.system.account_service.dtos.branch;

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
public class BranchBankingDTO {
    @NotBlank(message = MessageKeys.MESSAGES_BLANK_BRANCH_NAME)
    private String branchName;

    @NotBlank(message = MessageKeys.MESSAGES_BLANK_BRANCH_ADDRESS)
    private String address;

    private String description;
}
