package com.system.account_service.services.impl;

import com.system.account_service.dtos.loan.CreateLoanDTO;
import com.system.account_service.dtos.loan.LoanRp;
import com.system.account_service.dtos.loan.ReportLoanByRangeDTO;
import com.system.account_service.dtos.response.PageDataDTO;
import com.system.account_service.entities.AccountCommons;
import com.system.account_service.entities.BankingAccount;
import com.system.account_service.entities.LoanAccount;
import com.system.account_service.entities.type.AccountStatus;
import com.system.account_service.entities.type.AccountTypes;
import com.system.account_service.exception.payload.ExistedDataException;
import com.system.account_service.exception.payload.InvalidParamException;
import com.system.account_service.exception.payload.ResourceNotFoundException;
import com.system.account_service.repositories.LoanRepository;
import com.system.account_service.services.AccountCommonService;
import com.system.account_service.services.BankingAccountService;
import com.system.account_service.services.BranchBankingService;
import com.system.account_service.services.LoanAccountService;
import com.system.account_service.utils.DateTimeUtils;
import com.system.account_service.utils.MessageKeys;
import com.system.common_library.dto.report.AccountReportRequest;
import com.system.common_library.dto.response.account.AccountInfoDTO;
import com.system.common_library.enums.AccountType;
import com.system.common_library.enums.ObjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanAccountServiceImpl implements LoanAccountService {
    private final AccountCommonService accountCommonService;
    private final BankingAccountService bankingAccountService;
    private final BranchBankingService branchBankingService;

    private final LoanRepository repository;


    @Override
    @Transactional
    public AccountInfoDTO create(CreateLoanDTO data) {
        try {
            // create account_detail => create LoanAccount
            if(!data.getAccountCommon().getAccountType()
                    .equals(AccountTypes.LOAN.name())
            ) {
                throw new InvalidParamException(MessageKeys.DATA_INVALID_ACCOUNT_TYPE);
            }

            AccountCommons accountCommons = accountCommonService.create(data.getAccountCommon());

            // get banking_account to create Loan
            BankingAccount bankingAccount = bankingAccountService.getDataId(data.getBankingAccountId());
            String branchId = data.getBranchId();

            LoanAccount loanAccount = LoanAccount.builder()
                    .accountCommon(accountCommons)
                    .bankingAccount(bankingAccount)
                    .branch(!branchId.isBlank() ? branchBankingService.findById(branchId) : null)
                    .build();

            LoanAccount createdData = repository.save(loanAccount);
            return convertAccountInfoDTO(createdData);
        }
        catch (Exception e) {
            if(e instanceof ExistedDataException) {
                throw new ExistedDataException(((ExistedDataException) e).getMsgKey());
            }
            throw new InvalidParamException(MessageKeys.DATA_CREATE_FAILURE);
        }
    }

    @Override
    public LoanAccount updateBalanceByCommonId(String commonId, BigDecimal amount) {
        LoanAccount loanAccount = findByCommonId(commonId);

        BigDecimal newBalance = loanAccount.getBalance().add(amount);

        if(newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidParamException(MessageKeys.BALANCE_INSUFFICIENT);
        }

        loanAccount.setBalance(newBalance);

        return repository.save(loanAccount);
    }

    @Override
//    @CacheEvict(value = "loans", key = "#id")
    public void delete(String id) {
        LoanAccount data = repository.findByAccountIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);

        data.setDeleted(true);
        repository.save(data);
    }

    @Override
//    @Cacheable(value = "loans", key = "#id", unless = "#result == null")
    public LoanRp findById(String id) {
        LoanAccount data = repository.findByAccountIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);

        return convertRp(data);
    }

    @Override
    public LoanAccount findByCommonId(String commonId) {
        return repository.findByAccountCommon_AccountCommonIdAndDeleted(commonId, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public PageDataDTO<LoanRp> findAll(Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page-1, pageSize, Sort.by("createdAt"));
        Page<LoanAccount> pageData = repository.findAllByDeleted(false, pageable);
        List<LoanRp> listData = pageData.stream()
                .map(this::convertRp)
                .toList();

        return PageDataDTO.<LoanRp> builder()
                .total(pageData.getTotalElements())
                .listData(listData)
                .build();
    }

    @Override
    public List<LoanAccount> findAllByBranchId(String branchId) {
        return repository.findAccountsByBranch_BranchId(branchId);
    }

    @Override
    public LoanAccount getDataId(String id) {
        return repository.findByAccountIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public List<LoanAccount> getReportsByRange(AccountReportRequest request) {
        ReportLoanByRangeDTO reportByRange = ReportLoanByRangeDTO.builder()
                .branchId(request.getBankBranch())
                .startBalance(BigDecimal.valueOf(request.getStartBalance()))
                .endBalance(BigDecimal.valueOf(request.getEndBalance()))
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .status(AccountStatus.valueOf(request.getStatus().name()))
                .build();

        return repository.getReportsByRange(reportByRange);
    }


    //    Convert sang model response
    private LoanRp convertRp(LoanAccount data) {
        return LoanRp.builder()
                .id(data.getAccountId())
                .accountCommon(data.getAccountCommon().getAccountCommonId())
                .bankingAccount(data.getBankingAccount().getAccountId())
                .branch(data.getBranch().getBranchId())
                .balance(data.getBalance().stripTrailingZeros().toPlainString())
                .createAt(DateTimeUtils.format(DateTimeUtils.DD_MM_YYYY_HH_MM, data.getCreatedAt()))
                .build();
    }

    private AccountInfoDTO convertAccountInfoDTO(LoanAccount data) {
        AccountCommons common = data.getAccountCommon();

        return AccountInfoDTO.builder()
                .accountId(data.getAccountId())
                .customerId(common.getCustomerId())
                .cifCode(common.getCifCode())
                .accountNumber(common.getAccountNumber())
                .currentAccountBalance(data.getBalance())
                .accountType(AccountType.valueOf(common.getAccountType().name()))
                .statusAccount(ObjectStatus.valueOf(common.getStatus().name()))
                .branchName( (data.getBranch() != null) ? data.getBranch().getBranchName() : null )
                .createdAt(common.getCreatedAt())
                .build();
    }
}
