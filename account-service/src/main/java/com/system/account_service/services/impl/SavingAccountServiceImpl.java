package com.system.account_service.services.impl;

import com.system.account_service.client.CoreAccountClient;
import com.system.account_service.dtos.account_common.CreateAccountCommonDTO;
import com.system.account_service.dtos.response.PageDataDTO;
import com.system.account_service.dtos.saving.CreateSavingDTO;
import com.system.account_service.dtos.saving.ReportSavingByRangeDTO;
import com.system.account_service.dtos.saving.SavingRp;
import com.system.account_service.entities.*;
import com.system.account_service.entities.type.AccountStatus;
import com.system.account_service.entities.type.Currency;
import com.system.account_service.exception.payload.ExistedDataException;
import com.system.account_service.exception.payload.InvalidParamException;
import com.system.account_service.exception.payload.ResourceNotFoundException;
import com.system.account_service.repositories.SavingRepository;
import com.system.account_service.services.*;
import com.system.account_service.utils.DateTimeUtils;
import com.system.account_service.utils.MessageKeys;
import com.system.common_library.dto.report.AccountReportRequest;
import com.system.common_library.dto.request.account.CreateAccountCoreDTO;
import com.system.common_library.dto.response.account.AccountCoreDTO;
import com.system.common_library.dto.response.customer.CustomerCoreDTO;
import com.system.common_library.dto.transaction.account.savings.CreateSavingsDisbursementTransactionDTO;
import com.system.common_library.dto.transaction.account.savings.TransactionSavingsResultDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.AccountType;
import com.system.common_library.service.TransactionDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingAccountServiceImpl implements SavingAccountService {
    private final AccountCommonService accountCommonService;
    private final BankingAccountService bankingAccountService;
    private final InterestRateService interestRateService;
    private final BranchBankingService branchBankingService;

    // dubbo service
    @DubboReference
    private final TransactionDubboService transactionDubboService;

    // feign client
    private final CoreAccountClient coreAccountClient;

    private final SavingRepository repository;

    @Override
    @Transactional
    public SavingRp create(CustomerCoreDTO coreCustomer, CustomerDetailDTO customer, CreateSavingDTO data) {
        try {
            // ***
            // Check banking account balance
            // ***
            BankingAccount bankingAccount = bankingAccountService.findByCifCode(data.getCifCode());
            if(bankingAccount.getBalance().compareTo(data.getBalance()) < 0) {
                throw new InvalidParamException(MessageKeys.BALANCE_INSUFFICIENT);
            }

            // ***
            // Build DTO create SavingsAccount trong Core
            // ***
            CreateAccountCoreDTO createCoreData = CreateAccountCoreDTO.builder()
                    .cifCode(coreCustomer.getCifCode())
                    .currency(Currency.VND.name())
                    .isActive(true)
                    .type(AccountType.SAVINGS)
                    .build();

            // ***
            // Call API Create Account CoreBanking
            // ***
            ResponseEntity<AccountCoreDTO> resCoreAccount = coreAccountClient.create(createCoreData);
            AccountCoreDTO coreSavingsAccount = resCoreAccount.getBody();
            if(coreSavingsAccount == null){
                throw new InvalidParamException(MessageKeys.DATA_CREATE_FAILURE);
            }

            // ***
            // Build DTO Save account vao AccountService Database
            // 1. Create Account common
            // 2. Find Banking account
            // 3. Create SavingsAccount
            // ***
            CreateAccountCommonDTO createCommon = CreateAccountCommonDTO.builder()
                    .customerId(customer.getCustomerId())
                    .cifCode(coreCustomer.getCifCode())
                    .accountNumber(coreSavingsAccount.getAccount())
                    .accountType(coreSavingsAccount.getAccountType().name())
                    .status(AccountStatus.ACTIVE.name())
                    .createdAt(coreSavingsAccount.getDateCreated())
                    .updatedAt(coreSavingsAccount.getDateUpdated())
                    .build();

            InterestRates interestRate = interestRateService.getLowestRateForSavingTerm(data.getTerm());
            BranchBanking branch = branchBankingService.findById(data.getBranchId());
            AccountCommons accountCommons = accountCommonService.create(createCommon);

            // ***
            // build DTO create Transaction send money to SavingsAccount
            // ***
            CreateSavingsDisbursementTransactionDTO disbursementSavingDTO = CreateSavingsDisbursementTransactionDTO
                    .builder()
                    .cifCode(data.getCifCode())
                    .paymentAccount(bankingAccount.getAccountCommon().getAccountNumber())
                    .savingAccount(coreSavingsAccount.getAccount())
                    .amount(data.getBalance())
                    .build();

            TransactionSavingsResultDTO createSavingTransaction = transactionDubboService.createSavingsPaymentTransaction(disbursementSavingDTO);

            // ***
            // save account to internal db
            // ***
            Date endDate = DateTimeUtils.localDateTimeToDate(coreSavingsAccount.getDateCreated().plusMonths(data.getTerm()));
            SavingAccount savingAccount = SavingAccount.builder()
                    .accountCommon(accountCommons)
                    .bankingAccount(bankingAccount)
                    .interestRate(interestRate)
                    .branch(branch)
                    .balance(createSavingTransaction.getBalanceSavingsAccount())
                    .endDate(endDate)
                    .build();

            SavingAccount savedData = repository.save(savingAccount);
            return convertRp(savedData);
        }
        catch (Exception e) {
            if(e instanceof ExistedDataException) {
                throw new ExistedDataException(((ExistedDataException) e).getMsgKey());
            }
            throw new InvalidParamException(MessageKeys.DATA_CREATE_FAILURE);
        }
    }

    @Override
//    @CachePut(value = "savings", key = "#id")
    public void updateBalance(String id, BigDecimal balance) {
        SavingAccount savingAccount = repository.findByAccountIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);

        savingAccount.setBalance(balance);

        repository.save(savingAccount);
    }

    public SavingAccount updateBalanceByCommonId(String commonId, BigDecimal amount) {
        SavingAccount savingAccount = findByCommonId(commonId);

        BigDecimal newBalance = savingAccount.getBalance().add(amount);

        if(newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidParamException(MessageKeys.BALANCE_INSUFFICIENT);
        }

        savingAccount.setBalance(newBalance);

        return repository.save(savingAccount);
    }

    @Override
//    @CacheEvict(value = "savings", key = "#id")
    public void delete(String id) {
        SavingAccount data = repository.findByAccountIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);

        data.setDeleted(true);
        repository.save(data);
    }

    @Override
//    @Cacheable(value = "savings", key = "#id", unless = "#result == null")
    public SavingRp findById(String id) {
        SavingAccount data = repository.findByAccountIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);

        return convertRp(data);
    }

    @Override
    public SavingAccount findByCommonId(String commonId) {
        return repository.findByAccountCommon_AccountCommonIdAndDeleted(commonId, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public PageDataDTO<SavingRp> findAll(Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page-1, pageSize, Sort.by("createdAt"));
        Page<SavingAccount> pageData = repository.findAllByDeleted(false, pageable);
        List<SavingRp> listData = pageData.stream()
                .map(this::convertRp)
                .toList();

        return PageDataDTO.<SavingRp> builder()
                .total(pageData.getTotalElements())
                .listData(listData)
                .build();
    }

    @Override
    public List<SavingAccount> findAllByBranchId(String branchId) {
        return repository.findAccountsByBranch_BranchId(branchId);
    }

    @Override
    public SavingAccount getDataId(String id) {
        return repository.findByAccountIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public List<SavingAccount> getReportsByRange(AccountReportRequest request) {
        ReportSavingByRangeDTO reportByRange = ReportSavingByRangeDTO.builder()
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
    private SavingRp convertRp(SavingAccount data) {
        return SavingRp.builder()
                .id(data.getAccountId())
                .bankingAccount(data.getBankingAccount().getAccountCommon().getAccountNumber())
                .accountCommon(data.getAccountCommon().getAccountCommonId())
                .interestRate(data.getInterestRate().getRate().stripTrailingZeros().toPlainString())
                .branch(data.getBranch().getBranchName())
                .balance(data.getBalance().stripTrailingZeros().toPlainString())
                .endDate(DateTimeUtils.format(DateTimeUtils.DD_MM_YYYY_HH_MM, data.getEndDate()))
                .createAt(DateTimeUtils.format(DateTimeUtils.DD_MM_YYYY_HH_MM, data.getCreatedAt()))
                .build();
    }
}
