package com.system.common_library.dto.response.account;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class BranchInfoDTO implements Serializable {
    private String branchId;
    private String branchName;
    private String address;
    private String description;
}
