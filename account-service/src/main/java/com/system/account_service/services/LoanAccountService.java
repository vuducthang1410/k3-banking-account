package com.system.account_service.services;

import com.system.account_service.dtos.loan.CreateLoanDTO;
import com.system.account_service.dtos.loan.LoanRp;
import com.system.account_service.dtos.response.PageDataDTO;
import com.system.account_service.entities.LoanAccount;
import com.system.common_library.dto.report.AccountReportRequest;
import com.system.common_library.dto.response.account.AccountInfoDTO;

import java.math.BigDecimal;
import java.util.List;

public interface LoanAccountService {
    AccountInfoDTO create(CreateLoanDTO data);

    LoanAccount updateBalanceByCommonId(String commonId, BigDecimal amount);

    void delete(String id);

    LoanRp findById(String id);

    LoanAccount findByCommonId(String commonId);

    PageDataDTO<LoanRp> findAll(Integer page, Integer pageSize);

    List<LoanAccount> findAllByBranchId(String branchId);

    LoanAccount getDataId(String id);

    List<LoanAccount> getReportsByRange(AccountReportRequest request);
}
