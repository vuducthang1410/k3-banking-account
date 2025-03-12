package com.system.account_service.services;

import com.system.account_service.dtos.banking.BankingRp;
import com.system.account_service.dtos.banking.CreateBankingDTO;
import com.system.account_service.dtos.response.PageDataDTO;
import com.system.account_service.entities.BankingAccount;
import com.system.common_library.dto.report.AccountReportRequest;
import com.system.common_library.dto.response.account.AccountInfoDTO;

import java.math.BigDecimal;
import java.util.List;

public interface BankingAccountService {
    AccountInfoDTO create(CreateBankingDTO data);

    BankingRp updateBalance(String id, BigDecimal balance);

    BankingAccount updateBalanceByCommonId(String commonId, BigDecimal amount);

    void delete(String id);

    BankingRp findById(String id);

    BankingAccount findByCifCode(String cifCode);

    BankingAccount findByCommonId(String commonId);

    PageDataDTO<BankingRp> findAll(Integer page, Integer pageSize);

    List<BankingAccount> findAllByBranchId(String branchId);

    BankingAccount getDataId(String id);

    List<BankingAccount> getReportsByRange(AccountReportRequest request);
}
