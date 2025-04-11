package com.system.account_service.dtos.banking;

import lombok.Data;

@Data
public class BankAccountInfoRp {
    private String accountId;
    private String accountNumber;
    private String accountName;
    private String accountType;
}
