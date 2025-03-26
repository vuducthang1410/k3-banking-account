package com.system.common_library.service;

import com.system.common_library.dto.account.*;
import com.system.common_library.dto.report.AccountReportRequest;
import com.system.common_library.dto.report.AccountReportResponse;
import com.system.common_library.dto.response.account.AccountBalanceDTO;
import com.system.common_library.dto.response.account.AccountInfoDTO;
import com.system.common_library.dto.response.account.BranchInfoDTO;
import com.system.common_library.dto.response.account.LoanAccountInfoDTO;
import com.system.common_library.enums.AccountType;
import com.system.common_library.enums.ObjectStatus;
import com.system.common_library.exception.DubboException;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDubboService {

    AccountInfoDTO getAccountDetail(String account);
    AccountInfoDTO getBankingAccount(String cifCode);
    AccountInfoDTO getLoanAccountDTO(String loanAccountId);
    List<AccountInfoDTO> getAccountsByCifCode(String cifCode);
    List<AccountInfoDTO> getAccountsByBranchId(String branchId, List<AccountType> type);

    /* Todo: Create BankingAccount (Payment)
         CustomerService call this method */
    AccountInfoDTO createBankingAccount(CreateDubboBankingDTO data);

    AccountInfoDTO createSavingAccount(String cifCode, CreateDubboSavingDTO data);
    AccountInfoDTO createCreditAccount(String cifCode, CreateDubboCreditDTO data);

    /* Todo: Create LoanAccount (Loan)
         LoanService call this method */
    LoanAccountInfoDTO createLoanAccount(CreateDubboLoanDTO data);

    AccountBalanceDTO updateBalance(String account, BigDecimal amount);
    AccountInfoDTO updateAccountInfo(String account, UpdateAccountDTO data);
    AccountInfoDTO updateAccountStatus(String accountNumber, ObjectStatus status);
    boolean deleteAccountService(String accountNumber);
    //gRPC for reporting
    AccountReportResponse getReportAccount(String account,AccountType type) throws DubboException;
    List<AccountReportResponse> getReportAccounts(AccountReportRequest request) throws DubboException;
    List<AccountReportResponse> getReportAccountsByList(List<String> accounts) throws DubboException;

    BranchInfoDTO getRandomBranch();
}
