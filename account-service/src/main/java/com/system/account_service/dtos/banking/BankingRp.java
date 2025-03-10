package com.system.account_service.dtos.banking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankingRp implements Serializable {
    private String id;
    private String nickName;
    private String accountCommon;
    private String balance;
    private String branch;
    private String createAt;
}
