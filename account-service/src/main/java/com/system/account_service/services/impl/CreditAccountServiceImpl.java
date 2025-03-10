package com.system.account_service.services.impl;

import com.system.account_service.client.CoreAccountClient;
import com.system.account_service.dtos.account_common.CreateAccountCommonDTO;
import com.system.account_service.dtos.credit.CreateCreditDTO;
import com.system.account_service.dtos.credit.CreditRp;
import com.system.account_service.dtos.response.PageDataDTO;
import com.system.account_service.entities.*;
import com.system.account_service.entities.type.AccountStatus;
import com.system.account_service.entities.type.Currency;
import com.system.account_service.exception.payload.ExistedDataException;
import com.system.account_service.exception.payload.InvalidParamException;
import com.system.account_service.exception.payload.ResourceNotFoundException;
import com.system.account_service.repositories.CreditRepository;
import com.system.account_service.services.*;
import com.system.account_service.utils.DateTimeUtils;
import com.system.account_service.utils.MessageKeys;
import com.system.common_library.dto.request.account.CreateAccountCoreDTO;
import com.system.common_library.dto.response.account.AccountCoreDTO;
import com.system.common_library.dto.response.customer.CustomerCoreDTO;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditAccountServiceImpl implements CreditAccountService {
    private final AccountCommonService accountCommonService;
    private final BankingAccountService bankingAccountService;
    private final InterestRateService interestRateService;
    private final BranchBankingService branchBankingService;

    // dubbo service
    @DubboReference
    private final TransactionDubboService transactionDubboService;

    // feign client
    private final CoreAccountClient coreAccountClient;

    private final CreditRepository repository;


    @Override
    @Transactional
    public CreditRp create(CustomerCoreDTO coreCustomer, CustomerDetailDTO customer, CreateCreditDTO data) {
        try {
            // ***
            // Build DTO create CreditAccount trong Core
            // ***
            CreateAccountCoreDTO createCoreData = CreateAccountCoreDTO.builder()
                    .cifCode(coreCustomer.getCifCode())
                    .currency(Currency.VND.name())
                    .isActive(false)
                    .type(AccountType.CREDIT)
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
            // 3. Create LoanAccount
            // ***
            CreateAccountCommonDTO createCommon = CreateAccountCommonDTO.builder()
                    .customerId(customer.getCustomerId())
                    .cifCode(coreCustomer.getCifCode())
                    .accountNumber(coreSavingsAccount.getAccount())
                    .accountType(coreSavingsAccount.getAccountType().name())
                    .status(AccountStatus.SUSPENDED.name())
                    .createdAt(coreSavingsAccount.getDateCreated())
                    .updatedAt(coreSavingsAccount.getDateUpdated())
                    .build();

            BankingAccount bankingAccount = bankingAccountService.findByCifCode(data.getCifCode());
            InterestRates interestRate = interestRateService.getHighestRateForSavingTerm();
            BranchBanking branch = branchBankingService.findById(data.getBranchId());
            AccountCommons accountCommons = accountCommonService.create(createCommon);

            // ***
            // save account to internal db
            // ***
            CreditAccount creditAccount = CreditAccount.builder()
                    .accountCommon(accountCommons)
                    .bankingAccount(bankingAccount)
                    .interestRate(interestRate)
                    .branch(branch)
                    .build();

            CreditAccount savedData = repository.save(creditAccount);
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
    public void updateDebtBalance(String id, BigDecimal debtBalance) {
        CreditAccount creditAccount = repository.findByAccountIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);

        creditAccount.setDebtBalance(debtBalance);

        repository.save(creditAccount);
    }

    @Override
    public CreditAccount updateBalanceByCommonId(String commonId, BigDecimal amount) {
        CreditAccount creditAccount = findByCommonId(commonId);

        if(creditAccount.getAvailableBalance().compareTo(amount) < 0) {
            throw new InvalidParamException(MessageKeys.BALANCE_INSUFFICIENT);
        }

        BigDecimal newDebtBalance = creditAccount.getDebtBalance().subtract(amount);
        creditAccount.setDebtBalance(newDebtBalance);

        return repository.save(creditAccount);
    }

    @Override
//    @CacheEvict(value = "credits", key = "#id")
    public void delete(String id) {
        CreditAccount data = repository.findByAccountIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);

        data.setDeleted(true);
        repository.save(data);
    }

    @Override
//    @Cacheable(value = "credits", key = "#id", unless = "#result == null")
    public CreditRp findById(String id) {
        CreditAccount data = repository.findByAccountIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);

        return convertRp(data);
    }

    @Override
    public CreditAccount findByCommonId(String commonId) {
        return repository.findByAccountCommon_AccountCommonIdAndDeleted(commonId, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public PageDataDTO<CreditRp> findAll(Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page-1, pageSize, Sort.by("createdAt"));
        Page<CreditAccount> pageData = repository.findAllByDeleted(false, pageable);
        List<CreditRp> listData = pageData.stream()
                .map(this::convertRp)
                .toList();

        return PageDataDTO.<CreditRp> builder()
                .total(pageData.getTotalElements())
                .listData(listData)
                .build();
    }

    @Override
    public List<CreditAccount> findAllByBranchId(String branchId) {
        return repository.findAccountsByBranch_BranchId(branchId);
    }

    @Override
    public CreditAccount getDataId(String id) {
        return repository.findByAccountIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    //    Convert sang model response
    private CreditRp convertRp(CreditAccount data) {
        return CreditRp.builder()
                .id(data.getAccountId())
                .bankingAccount(data.getBankingAccount().getAccountCommon().getAccountNumber())
                .accountCommon(data.getAccountCommon().getAccountCommonId())
                .interestRate(data.getInterestRate().getRate().stripTrailingZeros().toPlainString())
                .branch(data.getBranch().getBranchName())
                .creditLimit(data.getCreditLimit().stripTrailingZeros().toPlainString())
                .debtBalance(data.getDebtBalance().stripTrailingZeros().toPlainString())
                .billingCycle(data.getBillingCycle().toString())
                .lastPaymentDate(DateTimeUtils.format(DateTimeUtils.DD_MM_YYYY_HH_MM, data.getLastPaymentDate()))
                .createAt(DateTimeUtils.format(DateTimeUtils.DD_MM_YYYY_HH_MM, data.getCreatedAt()))
                .build();
    }
}
