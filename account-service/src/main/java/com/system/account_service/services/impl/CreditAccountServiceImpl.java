package com.system.account_service.services.impl;

import com.system.account_service.client.CoreAccountClient;
import com.system.account_service.dtos.account_common.CreateAccountCommonDTO;
import com.system.account_service.dtos.credit.CreateCreditDTO;
import com.system.account_service.dtos.credit.CreditProfileDTO;
import com.system.account_service.dtos.credit.CreditRp;
import com.system.account_service.dtos.credit.ReportCreditByRangeDTO;
import com.system.account_service.dtos.response.PageDataDTO;
import com.system.account_service.entities.*;
import com.system.account_service.entities.type.AccountStatus;
import com.system.account_service.entities.type.Currency;
import com.system.account_service.exception.payload.ExistedDataException;
import com.system.account_service.exception.payload.InvalidParamException;
import com.system.account_service.exception.payload.ResourceNotFoundException;
import com.system.account_service.repositories.CreditProfileRepository;
import com.system.account_service.repositories.CreditRepository;
import com.system.account_service.services.*;
import com.system.account_service.utils.DateTimeUtils;
import com.system.account_service.utils.MessageKeys;
import com.system.common_library.dto.report.AccountReportRequest;
import com.system.common_library.dto.request.account.CreateAccountCoreDTO;
import com.system.common_library.dto.request.account.UpdateAccountCoreDTO;
import com.system.common_library.dto.response.account.AccountCoreDTO;
import com.system.common_library.dto.response.customer.CustomerCoreDTO;
import com.system.common_library.dto.transaction.account.credit.CreateCreditDisbursementTransactionDTO;
import com.system.common_library.dto.transaction.account.credit.TransactionCreditResultDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.AccountType;
import com.system.common_library.service.CustomerDubboService;
import com.system.common_library.service.TransactionDubboService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

    @DubboReference
    private final CustomerDubboService customerDubboService;

    // feign client
    private final CoreAccountClient coreAccountClient;

    private final CreditRepository repository;
    private final CreditProfileRepository creditProfileRepository;


    /*
    * Todo: Create Credit Account
    *  * Tao mac dinh voi Status = SUSPENDED
    *   => Can Activation, tinh toan Credit-Limit
    * */
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

    /*
    * Todo: Active Credit Account
    *  B1: Report Thu nhap ca nhan (income)
    *  B2: Tinh toan Credit_limit dua tren income
    *  B3: Update Credit_limit trong Core & update Status -> ACTIVE
    * */
    @Override
    @Transactional
    public CreditRp active(String creditId, String customerId, CreditProfileDTO profile) {
        CreditAccount creditAccount = getDataId(creditId);
        AccountCommons accountCommons = accountCommonService.findById(creditAccount.getAccountCommon().getAccountCommonId());
        BigDecimal creditLimit = creditLimitGenerator(profile);

        String errMsg = "";
        try {
            errMsg = "get Customer & update Credit profile ERR";
            CustomerDetailDTO customer = customerDubboService.getCustomerByCustomerId(customerId);

            CreditProfiles saveProfile = CreditProfiles.builder()
                .customerId(customer.getCustomerId())
                .company(profile.getCompany())
                .phone(profile.getPhone())
                .income(profile.getIncome())
                .build();
            creditProfileRepository.save(saveProfile);

            // update status trong Core
            errMsg = "Update status Credit Core ERR";
            UpdateAccountCoreDTO updateCoreDTO = UpdateAccountCoreDTO.builder()
                    .currency(Currency.VND.name())
                    .isActive(true)
                    .build();

            coreAccountClient.update(
                    creditAccount.getAccountCommon().getAccountNumber(),
                    updateCoreDTO
            );

            // giai ngan cho tk credit
            errMsg = "Disbursement to credit account ERR";
            CreateCreditDisbursementTransactionDTO creditDisbursement =
                    CreateCreditDisbursementTransactionDTO.builder()
                            .cifCode(customer.getCifCode())
                            .amount(creditLimit)
                            .creditAccount(creditAccount.getAccountCommon().getAccountNumber())
                            .build();
            TransactionCreditResultDTO resultDTO =
                    transactionDubboService.createCreditAccountDisbursement(creditDisbursement);

            // update internal db
            errMsg = "Update internal DB ERR";
            accountCommons.setStatus(AccountStatus.ACTIVE);
            creditAccount.setCreditLimit(creditLimit);
            creditAccount.setAccountCommon(accountCommons);
            repository.save(creditAccount);

            errMsg = null;
            return convertRp(creditAccount);
        }
        catch (Exception e) {
            if(errMsg != null){
                log.error(errMsg, e.getMessage());
            }
            throw new InvalidParamException(MessageKeys.CREDIT_ACTIVE_FAILURE);
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

    @Override
    public List<CreditAccount> getReportsByRange(AccountReportRequest request) {
        ReportCreditByRangeDTO reportByRange = ReportCreditByRangeDTO.builder()
                .branchId(request.getBankBranch())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .status(AccountStatus.valueOf(request.getStatus().name()))
                .build();

        return repository.getReportsByRange(reportByRange);
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

    // Function tao Credit_Limit dua vao income info
    private BigDecimal creditLimitGenerator(CreditProfileDTO creditProfile) {
        /*
        * Xu ly tinh toan credit limit
        * */

        BigDecimal income = creditProfile.getIncome();
        return income.multiply(BigDecimal.valueOf(10));
    }
}
