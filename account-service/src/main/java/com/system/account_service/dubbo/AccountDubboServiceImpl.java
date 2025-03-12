package com.system.account_service.dubbo;

import com.system.account_service.client.CoreAccountClient;
import com.system.account_service.client.CoreCustomerClient;
import com.system.account_service.dtos.account_common.CreateAccountCommonDTO;
import com.system.account_service.dtos.banking.CreateBankingDTO;
import com.system.account_service.dtos.loan.CreateLoanDTO;
import com.system.account_service.entities.*;
import com.system.account_service.entities.type.AccountStatus;
import com.system.account_service.entities.type.AccountTypes;
import com.system.account_service.entities.type.Currency;
import com.system.account_service.exception.payload.InvalidParamException;
import com.system.account_service.exception.payload.ResourceNotFoundException;
import com.system.account_service.services.*;
import com.system.account_service.utils.MessageKeys;
import com.system.common_library.dto.account.*;
import com.system.common_library.dto.report.AccountReportRequest;
import com.system.common_library.dto.report.AccountReportResponse;
import com.system.common_library.dto.request.account.CreateAccountCoreDTO;
import com.system.common_library.dto.response.account.*;
import com.system.common_library.dto.response.customer.CustomerCoreDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.AccountType;
import com.system.common_library.enums.ObjectStatus;
import com.system.common_library.exception.DubboException;
import com.system.common_library.service.AccountDubboService;
import com.system.common_library.service.CustomerDubboService;
import com.system.common_library.util.ErrorMapGenerator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Slf4j
@DubboService
@RequiredArgsConstructor
public class AccountDubboServiceImpl implements AccountDubboService {

    // inject Service dependencies
    private final AccountCommonService accountCommonService;
    private final BankingAccountService bankingService;
    private final CreditAccountService creditService;
    private final SavingAccountService savingService;
    private final LoanAccountService loanService;
    private final BranchBankingService branchBankingService;

    // inject other dependencies
    private final CoreCustomerClient coreCustomerClient;
    private final CoreAccountClient coreAccountClient;
    private final MessageSource messageSource;
    private final Validator validator;
    private final RollbackService rollbackService;

    // Dubbo / gRPC service
    @DubboReference
    private final CustomerDubboService customerDubboService;

    /* todo: Get Account info by account_number
    *   response DTO: AccountInfoDTO */
    @Override
    public AccountInfoDTO getAccountDetail(String accountNumber) {
        try {
            /* Lay thong tin chung cua Account */
            AccountCommons common = accountCommonService.findByAccountNumber(accountNumber);

            return bindingAccountDTOFromCommon(common);
        }
        catch (Exception e) {
            // Neu khong tim thay data => return null
            if(e instanceof ResourceNotFoundException) {
                return null;
            }

            throw new DubboException(e.getMessage());
        }
    }

    /* Todo: Get Banking account info by CIF code
     *   Type: PAYMENT
     *   response DTO: AccountInfoDTO */
    @Override
    public AccountInfoDTO getBankingAccount(String cifCode) {
        try {
            BankingAccount bankingAccount = bankingService.findByCifCode(cifCode);

            return AccountInfoDTO
                    .builder()
                    .accountId(bankingAccount.getAccountId())
                    .accountNumber(bankingAccount.getAccountCommon().getAccountNumber())
                    .customerId(bankingAccount.getAccountCommon().getCustomerId())
                    .cifCode(bankingAccount.getAccountCommon().getCifCode())
                    .accountType(AccountType.valueOf(bankingAccount.getAccountCommon().getAccountType().name()))
                    .statusAccount(ObjectStatus.valueOf(bankingAccount.getAccountCommon().getStatus().name()))
                    .currentAccountBalance(bankingAccount.getBalance())
                    .branchName(bankingAccount.getBranch().getBranchName())
                    .createdAt(bankingAccount.getCreatedAt())
                    .build();
        }
        catch (Exception e) {
                if(e instanceof ResourceNotFoundException) {
                return null;
            }

            throw new DubboException(e.getMessage());
        }
    }

    /* Todo: Get Loan account info by ID
     *   Type: LOAN
     *   response DTO: AccountInfoDTO */
    @Override
    public AccountInfoDTO getLoanAccountDTO(String loanAccountId) {
        try {
            LoanAccount loanAccount = loanService.getDataId(loanAccountId);
            AccountCommons common = loanAccount.getAccountCommon();

            return AccountInfoDTO
                    .builder()
                    .accountId(loanAccount.getAccountId())
                    .accountNumber(common.getAccountNumber())
                    .customerId(common.getCustomerId())
                    .cifCode(common.getCifCode())
                    .accountType(AccountType.valueOf(common.getAccountType().name()))
                    .statusAccount(ObjectStatus.valueOf(common.getStatus().name()))
                    .currentAccountBalance(loanAccount.getBalance())
                    .branchName(loanAccount.getBranch().getBranchName())
                    .createdAt(loanAccount.getCreatedAt())
                    .build();
        }
        catch (Exception e) {
            if(e instanceof ResourceNotFoundException) {
                return null;
            }

            throw new DubboException(e.getMessage());
        }
    }

    /* todo: Get Account info by CIF code
     *   response DTO: AccountInfoDTO */
    @Override
    public List<AccountInfoDTO> getAccountsByCifCode(String cifCode) {
        try {
            /* Lay thong tin chung cua Account */
            List<AccountCommons> commons = accountCommonService.findAllByCifCode(cifCode);

            return commons.stream()
                    .map(this::bindingAccountDTOFromCommon)
                    .toList();
        }
        catch (Exception e) {
            // Neu khong tim thay data => return null
            if(e instanceof ResourceNotFoundException) {
                return null;
            }

            throw new DubboException(e.getMessage());
        }
    }

    /* todo: Get List Account info by branch ID & AccountType
     *   response type: Array AccountInfoDTO */
    @Override
    public List<AccountInfoDTO> getAccountsByBranchId(String branchId, List<AccountType> type) {

        List<AccountInfoDTO> result = new ArrayList<>();

        if(type.contains(AccountType.PAYMENT)){
            /// Get banking account
            List<BankingAccount> accounts = bankingService.findAllByBranchId(branchId);
            result = accounts.stream()
                    // Todo: Map data to convert BankingAccount -> AccountInfoDTO
                    .map(acc -> {
                        AccountCommons common = acc.getAccountCommon();

                        return AccountInfoDTO.builder()
                                .accountId(acc.getAccountId())
                                .accountNumber(common.getAccountNumber())
                                .customerId(common.getCustomerId())
                                .cifCode(common.getCifCode())
                                .accountType(AccountType.valueOf(common.getAccountType().name()))
                                .statusAccount(ObjectStatus.valueOf(common.getStatus().name()))
                                .currentAccountBalance(acc.getBalance())
                                .branchName(acc.getBranch().getBranchName())
                                .createdAt(acc.getCreatedAt())
                                .build();
                    })
                    .toList();
        }

        else if(type.contains(AccountType.SAVINGS)){
            /// Get savings account
            List<SavingAccount> accounts = savingService.findAllByBranchId(branchId);
            result = accounts.stream()
                    // Todo: Map data to convert SavingAccount -> AccountInfoDTO
                    .map(acc -> {
                        AccountCommons common = acc.getAccountCommon();

                        return AccountInfoDTO.builder()
                                .accountId(acc.getAccountId())
                                .accountNumber(common.getAccountNumber())
                                .customerId(common.getCustomerId())
                                .cifCode(common.getCifCode())
                                .accountType(AccountType.valueOf(common.getAccountType().name()))
                                .statusAccount(ObjectStatus.valueOf(common.getStatus().name()))
                                .currentAccountBalance(acc.getBalance())
                                .branchName(acc.getBranch().getBranchName())
                                .createdAt(acc.getCreatedAt())
                                .build();
                    })
                    .toList();
        }

        else if(type.contains(AccountType.LOAN)){
            /// Get loan account
            List<LoanAccount> accounts = loanService.findAllByBranchId(branchId);
            result = accounts.stream()
                    // Todo: Map data to convert LoanAccount -> AccountInfoDTO
                    .map(acc -> {
                        AccountCommons common = acc.getAccountCommon();

                        return AccountInfoDTO.builder()
                                .accountId(acc.getAccountId())
                                .accountNumber(common.getAccountNumber())
                                .customerId(common.getCustomerId())
                                .cifCode(common.getCifCode())
                                .accountType(AccountType.valueOf(common.getAccountType().name()))
                                .statusAccount(ObjectStatus.valueOf(common.getStatus().name()))
                                .currentAccountBalance(acc.getBalance())
                                .branchName(acc.getBranch().getBranchName())
                                .createdAt(acc.getCreatedAt())
                                .build();
                    })
                    .toList();
        }

        else if(type.contains(AccountType.CREDIT)){
            /// Get credit account
            List<CreditAccount> accounts = creditService.findAllByBranchId(branchId);
            result = accounts.stream()
                    // Todo: Map data to convert CreditAccount -> AccountInfoDTO
                    .map(acc -> {
                        AccountCommons common = acc.getAccountCommon();

                        return AccountInfoDTO.builder()
                                .accountId(acc.getAccountId())
                                .accountNumber(common.getAccountNumber())
                                .customerId(common.getCustomerId())
                                .cifCode(common.getCifCode())
                                .accountType(AccountType.valueOf(common.getAccountType().name()))
                                .statusAccount(ObjectStatus.valueOf(common.getStatus().name()))
                                .creditLimit(acc.getCreditLimit())
                                .debtBalance(acc.getDebtBalance())
                                .branchName(acc.getBranch().getBranchName())
                                .createdAt(acc.getCreatedAt())
                                .build();
                    })
                    .toList();
        }

        return result;
    }

    @Override
    public BranchInfoDTO getRandomBranch() {
        List<BranchBanking> list = branchBankingService.findAll();
        Random random = new Random();

        BranchBanking randomBranch = list.get(random.nextInt(list.size()));

        return BranchInfoDTO.builder()
                .branchId(randomBranch.getBranchId())
                .branchName(randomBranch.getBranchName())
                .address(randomBranch.getAddress())
                .description(randomBranch.getDescription())
                .build();
    }

    /* todo: Create a Banking account (Call by CustomerService)
     *   response type: AccountInfoDTO
     *   B1: Xac thuc Customer (Get Customer trong Core & kiem tra)
     *   B2: Tao Banking Account trong Core
     *   B3: Luu Account vao database cua AccountService */
    @Override
    @Transactional
    public AccountInfoDTO createBankingAccount(CreateDubboBankingDTO data) {
        // ***
        // Validate request
        // ***
        log.info("Entering [createBankingAccount] with parameters: create = {}", data.toString());
        Set<ConstraintViolation<Object>> violations = validator.validate(data);
        if (!violations.isEmpty()) {
            String errors = ErrorMapGenerator.violationToErrorMap(violations);
            log.error(errors);
            throw new DubboException(errors);
        }

        // ***
        // Lay thong tin Customer trong CoreBanking = CIF code
        // ***
        ResponseEntity<CustomerCoreDTO> resCoreCustomer = coreCustomerClient.getByCifCode(data.getCifCode());
        CustomerCoreDTO coreCustomer = resCoreCustomer.getBody();

        // ***
        // Xac thuc account exists & active
        // ***
        if(coreCustomer == null){
            throw new DubboException(
                    messageSource.getMessage(MessageKeys.DUBBO_CUSTOMER_INVALID, null, LocaleContextHolder.getLocale())
            );
        }
        if(!coreCustomer.getIsActive()) {
            throw new DubboException(
                    messageSource.getMessage(MessageKeys.DUBBO_CUSTOMER_NOT_ACTIVE, null, LocaleContextHolder.getLocale())
            );
        }
        // ***
        // Xac thuc Customer hop le (CIF code & Info hop le)
        // ***
        if(!(data.getFullName().equals(coreCustomer.getFullName()))
            || !(data.getPhone().equals(coreCustomer.getPhone()))
            || !(data.getEmail().equals(coreCustomer.getEmail()))
        ) {
            throw new DubboException(
                    messageSource.getMessage(MessageKeys.DUBBO_CUSTOMER_INVALID, null, LocaleContextHolder.getLocale())
            );
        }

        // ***
        // Build DTO createAccount trong Core
        // ***
        CreateAccountCoreDTO createData = CreateAccountCoreDTO.builder()
                .cifCode(data.getCifCode())
                .currency(Currency.VND.name())
                .isActive(true)
                .type(AccountType.PAYMENT)
                .build();

        // ***
        // Call API Create Account CoreBanking
        // ***
        ResponseEntity<AccountCoreDTO> resCoreAccount = coreAccountClient.create(createData);
        AccountCoreDTO coreAccount = resCoreAccount.getBody();
        if(coreAccount == null){
            throw new DubboException(
                    messageSource.getMessage(MessageKeys.DATA_CREATE_FAILURE, null, LocaleContextHolder.getLocale())
            );
        }

        // ***
        // Build DTO Save account vao AccountService Database
        // ***
        CreateAccountCommonDTO createCommon = CreateAccountCommonDTO.builder()
                .customerId(data.getCustomerId())
                .cifCode(data.getCifCode())
                .accountNumber(coreAccount.getAccount())
                .accountType(coreAccount.getAccountType().name())
                .status(AccountStatus.ACTIVE.name())
                .createdAt(coreAccount.getDateCreated())
                .updatedAt(coreAccount.getDateUpdated())
                .build();
        CreateBankingDTO createBankingData = CreateBankingDTO.builder()
                .accountCommon(createCommon)
                .branchId(data.getBranchId())
                .nickName(data.getFullName().toUpperCase())
                .build();

        // ***
        // Save in AccountService Database
        // ***
        try {
            return bankingService.create(createBankingData);
        }
        catch (Exception e) {
            rollbackService.rollbackCreateCoreBankingAccount(coreAccount.getId());
            throw new DubboException(
                    messageSource.getMessage(MessageKeys.DATA_CREATE_FAILURE, null, LocaleContextHolder.getLocale())
            );
        }
    }

    @Override
    @Transactional
    public AccountInfoDTO createSavingAccount(String customerId, CreateDubboSavingDTO data) {
        return null;
    }

    @Override
    @Transactional
    public AccountInfoDTO createCreditAccount(String customerId, CreateDubboCreditDTO data) {
        return null;
    }

    @Override
    @Transactional
    public LoanAccountInfoDTO createLoanAccount(CreateDubboLoanDTO data) {
        // ***
        // Validate request
        // ***
        log.info("Entering [createLoanAccount] with parameters: create = {}", data.toString());
        Set<ConstraintViolation<Object>> violations = validator.validate(data);
        if (!violations.isEmpty()) {
            String errors = ErrorMapGenerator.violationToErrorMap(violations);
            log.error(errors);
            throw new DubboException(errors);
        }

        // ***
        // Lay thong tin Customer trong CustomerService = CIF code
        // ***
        CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(data.getCifCode());

        // ***
        // Lay thong tin Customer trong CoreBanking = CIF code
        // ***
        ResponseEntity<CustomerCoreDTO> resCoreCustomer = coreCustomerClient.getByCifCode(data.getCifCode());
        CustomerCoreDTO coreCustomer = resCoreCustomer.getBody();

        // ***
        // Xac thuc account exists & active
        // ***
        if((coreCustomer == null) || (customer == null)) {
            throw new DubboException(
                    messageSource.getMessage(MessageKeys.DUBBO_CUSTOMER_INVALID, null, LocaleContextHolder.getLocale())
            );
        }
        if((!coreCustomer.getIsActive()) || !(customer.getStatus().equals(ObjectStatus.ACTIVE))) {
            throw new DubboException(
                    messageSource.getMessage(MessageKeys.DUBBO_CUSTOMER_NOT_ACTIVE, null, LocaleContextHolder.getLocale())
            );
        }
        // ***
        // Xac thuc Customer hop le (CIF code & Info hop le): So sanh 2 customer trong Service vs Core
        // ***
        if(!(customer.getFullName().equals(coreCustomer.getFullName()))
                || !(customer.getPhone().equals(coreCustomer.getPhone()))
                || !(customer.getMail().equals(coreCustomer.getEmail()))
        ) {
            throw new DubboException(
                    messageSource.getMessage(MessageKeys.DUBBO_CUSTOMER_INVALID, null, LocaleContextHolder.getLocale())
            );
        }

        // ***
        // Build DTO createAccount trong Core
        // ***
        CreateAccountCoreDTO createCoreData = CreateAccountCoreDTO.builder()
                .cifCode(coreCustomer.getCifCode())
                .currency(Currency.VND.name())
                .isActive(true)
                .type(AccountType.LOAN)
                .build();

        // ***
        // Call API Create Account CoreBanking
        // ***
        ResponseEntity<AccountCoreDTO> resCoreAccount = coreAccountClient.create(createCoreData);
        AccountCoreDTO coreAccount = resCoreAccount.getBody();
        if(coreAccount == null){
            throw new DubboException(
                    messageSource.getMessage(MessageKeys.DATA_CREATE_FAILURE, null, LocaleContextHolder.getLocale())
            );
        }

        // ***
        // Build DTO Save account vao AccountService Database
        // 1. Create Account common
        // 2. Find Banking account
        // 3. Create LoanAccount
        // ***
        CreateAccountCommonDTO createCommon = CreateAccountCommonDTO.builder()
                .customerId(data.getCustomerId())
                .cifCode(coreCustomer.getCifCode())
                .accountNumber(coreAccount.getAccount())
                .accountType(coreAccount.getAccountType().name())
                .status(AccountStatus.ACTIVE.name())
                .createdAt(coreAccount.getDateCreated())
                .updatedAt(coreAccount.getDateUpdated())
                .build();

        BankingAccount bankingAccount = bankingService.findByCifCode(data.getCifCode());

        CreateLoanDTO createLoanData = CreateLoanDTO.builder()
                .accountCommon(createCommon)
                .bankingAccountId(bankingAccount.getAccountId())
                .branchId( (data.getBranchId() != null) ? data.getBranchId() : null )
                .build();

        // ***
        // Save in AccountService Database
        // ***
        AccountInfoDTO saved = loanService.create(createLoanData);
        return LoanAccountInfoDTO.builder()
                .accountLoanId(saved.getAccountId())
                .loanAccountNumber(saved.getAccountNumber())
                .statusLoanAccount(saved.getStatusAccount())
                .loanBalance(saved.getCurrentAccountBalance().stripTrailingZeros().toPlainString())
                .build();
    }

    @Override
    @Transactional
    public AccountBalanceDTO updateBalance(String accountNumber, BigDecimal amount) {
        try {
            /*
                Lay thong tin chung cua Account
            */
            AccountCommons common = accountCommonService.findByAccountNumber(accountNumber);

            /*
                Update Balance theo Account Type
            */
            AccountBalanceDTO response = new AccountBalanceDTO();
            if(common.getAccountType().equals(AccountTypes.PAYMENT)) {
                BankingAccount bankingAccount = bankingService.updateBalanceByCommonId(common.getAccountCommonId(), amount);

                response.setBalance(bankingAccount.getBalance());
                response.setAccountNumber(accountNumber);
            }
            else if(common.getAccountType().equals(AccountTypes.CREDIT)) {
                CreditAccount creditAccount = creditService.updateBalanceByCommonId(common.getAccountCommonId(), amount);

                response.setBalance(creditAccount.getAvailableBalance());
                response.setAccountNumber(accountNumber);
            }
            else if(common.getAccountType().equals(AccountTypes.SAVINGS)) {
                SavingAccount savingAccount = savingService.updateBalanceByCommonId(common.getAccountCommonId(), amount);

                response.setBalance(savingAccount.getBalance());
                response.setAccountNumber(accountNumber);
            }
            else if(common.getAccountType().equals(AccountTypes.LOAN)) {
                LoanAccount loanAccount = loanService.updateBalanceByCommonId(common.getAccountCommonId(), amount);

                response.setBalance(loanAccount.getBalance());
                response.setAccountNumber(accountNumber);
            }

            return response;
        }
        catch (Exception e) {
            // Neu khong tim thay data => return null
            if(e instanceof ResourceNotFoundException) {
                return null;
            }

            if(e instanceof InvalidParamException) {
                throw new DubboException(
                        messageSource.getMessage(MessageKeys.BALANCE_INSUFFICIENT, null, LocaleContextHolder.getLocale())
                );
            }

            throw new DubboException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public AccountInfoDTO updateAccountInfo(String accountId, UpdateAccountDTO data) {
        return null;
    }

    @Override
    @Transactional
    public AccountInfoDTO updateAccountStatus(String accountNumber, ObjectStatus status) {
        try {
            /* Lay thong tin chung cua Account */
            AccountCommons common = accountCommonService.updateStatusByAccountNumber(accountNumber, status.name());

            return bindingAccountDTOFromCommon(common);
        }
        catch (Exception e) {
            // Neu khong tim thay data => return null
            if(e instanceof ResourceNotFoundException) {
                return null;
            }

            throw new DubboException(e.getMessage());
        }
    }


    /* todo: Get Report data to pass to ReportService
     *   response DTO: AccountReportResponse */
    @Override
    public AccountReportResponse getReportAccount(String accountNumber, AccountType type) {
        try {
            // Lay thong tin chung cua Account
            AccountCommons common = accountCommonService.findByAccountNumber(accountNumber);

            // Convert sang AccountInfoDTO
            AccountInfoDTO accountInfo = bindingAccountDTOFromCommon(common);

            // Convert sang AccountReportResponse & return
            return AccountReportResponse.builder()
                    .customerId(common.getCustomerId())
                    .accountNumber(accountInfo.getAccountNumber())
                    .accountType(accountInfo.getAccountType())
                    .status(ObjectStatus.valueOf(common.getStatus().name()))
                    .bankBranch(accountInfo.getBranchName())
                    .balance(accountInfo.getCurrentAccountBalance().doubleValue())
                    .openedAt(accountInfo.getCreatedAt())
                    .creditLimit( (accountInfo.getCreditLimit() != null) ? (accountInfo.getCreditLimit().stripTrailingZeros().toPlainString()) : null )
                    .debtBalance((accountInfo.getDebtBalance() != null) ? (accountInfo.getDebtBalance().stripTrailingZeros().toPlainString()) : null)
                    .rate( (accountInfo.getInterestRate() != null) ? (accountInfo.getInterestRate().doubleValue()) : null )
                    .billingCycle( (accountInfo.getBillingCycle() != null) ? accountInfo.getBillingCycle() : null )
                    .build();
        }
        catch (Exception e) {
            // Neu khong tim thay data => return null
            if(e instanceof ResourceNotFoundException) {
                return null;
            }

            throw new DubboException(e.getMessage());
        }
    }

    @Override
    public List<AccountReportResponse> getReportAccounts(AccountReportRequest request) {
        try {
            AccountTypes type = AccountTypes.valueOf(request.getAccountType().name());

            if(type.equals(AccountTypes.PAYMENT)) {
                List<BankingAccount> listAcc = bankingService.getReportsByRange(request);

                return listAcc.stream()
                    .map(b ->
                        AccountReportResponse.builder()
                            .customerId(b.getAccountCommon().getCustomerId())
                            .accountNumber(b.getAccountCommon().getAccountNumber())
                            .accountType(AccountType.valueOf(b.getAccountCommon().getAccountType().name()))
                            .status(ObjectStatus.valueOf(b.getAccountCommon().getStatus().name()))
                            .bankBranch(b.getBranch().getBranchName())
                            .balance(b.getBalance().doubleValue())
                            .openedAt(b.getCreatedAt())
                            .build())
                .toList();
            }

            if(type.equals(AccountTypes.CREDIT)) {
                List<CreditAccount> listAcc = creditService.getReportsByRange(request);

                return listAcc.stream()
                    .map(b ->
                        AccountReportResponse.builder()
                            .customerId(b.getAccountCommon().getCustomerId())
                            .accountNumber(b.getAccountCommon().getAccountNumber())
                            .accountType(AccountType.valueOf(b.getAccountCommon().getAccountType().name()))
                            .status(ObjectStatus.valueOf(b.getAccountCommon().getStatus().name()))
                            .bankBranch(b.getBranch().getBranchName())
                            .openedAt(b.getCreatedAt())
                            .creditLimit(b.getCreditLimit().stripTrailingZeros().toPlainString())
                            .debtBalance(b.getDebtBalance().stripTrailingZeros().toPlainString())
                            .rate(b.getInterestRate().getRate().doubleValue())
                            .billingCycle(b.getBillingCycle())
                            .build())
                    .toList();
            }

            if(type.equals(AccountTypes.SAVINGS)) {
                List<SavingAccount> listAcc = savingService.getReportsByRange(request);

                return listAcc.stream()
                    .map(b ->
                        AccountReportResponse.builder()
                            .customerId(b.getAccountCommon().getCustomerId())
                            .accountNumber(b.getAccountCommon().getAccountNumber())
                            .accountType(AccountType.valueOf(b.getAccountCommon().getAccountType().name()))
                            .status(ObjectStatus.valueOf(b.getAccountCommon().getStatus().name()))
                            .bankBranch(b.getBranch().getBranchName())
                            .balance(b.getBalance().doubleValue())
                            .openedAt(b.getCreatedAt())
                            .rate(b.getInterestRate().getRate().doubleValue())
                            .build())
                    .toList();
            }

            if(type.equals(AccountTypes.LOAN)) {
                List<LoanAccount> listAcc = loanService.getReportsByRange(request);

                return listAcc.stream()
                    .map(b ->
                        AccountReportResponse.builder()
                            .customerId(b.getAccountCommon().getCustomerId())
                            .accountNumber(b.getAccountCommon().getAccountNumber())
                            .accountType(AccountType.valueOf(b.getAccountCommon().getAccountType().name()))
                            .status(ObjectStatus.valueOf(b.getAccountCommon().getStatus().name()))
                            .bankBranch(b.getBranch().getBranchName())
                            .balance(b.getBalance().doubleValue())
                            .openedAt(b.getCreatedAt())
                            .build())
                    .toList();
            }

            return List.of();
        }
        catch (Exception e) {
            log.error(e.getMessage());
            throw new DubboException(e.getMessage());
        }
    }

    @Override
    public List<AccountReportResponse> getReportAccountsByList(List<String> accounts) throws DubboException {
        return List.of();
    }

    /* todo: Get Account info from Common data (AccountCommon)
         -> Convert to AccountInfoDTO
     *   response DTO: AccountReportResponse */
    private AccountInfoDTO bindingAccountDTOFromCommon(AccountCommons common) {
        AccountInfoDTO accountInfo = AccountInfoDTO
                .builder()
                .accountNumber(common.getAccountNumber())
                .customerId(common.getCustomerId())
                .cifCode(common.getCifCode())
                .accountType(AccountType.valueOf(common.getAccountType().name()))
                .statusAccount(ObjectStatus.valueOf(common.getStatus().name()))
                .build();

        // Lay thong tin rieng cua tung loai Account (dua theo AccountType)
        if(common.getAccountType().equals(AccountTypes.PAYMENT)) {
            BankingAccount bankingAccount = bankingService.findByCommonId(common.getAccountCommonId());

            accountInfo.setCurrentAccountBalance(bankingAccount.getBalance());
            accountInfo.setAccountId(bankingAccount.getAccountId());
            accountInfo.setBranchName(bankingAccount.getBranch().getBranchName());
            accountInfo.setCreatedAt(bankingAccount.getCreatedAt());
        }
        else if(common.getAccountType().equals(AccountTypes.CREDIT)) {
            CreditAccount creditAccount = creditService.findByCommonId(common.getAccountCommonId());

            accountInfo.setAccountId(creditAccount.getAccountId());
            accountInfo.setBranchName(creditAccount.getBranch().getBranchName());
            accountInfo.setCreditLimit(creditAccount.getCreditLimit());
            accountInfo.setDebtBalance(creditAccount.getDebtBalance());
            accountInfo.setBillingCycle(creditAccount.getBillingCycle());
            accountInfo.setInterestRate(creditAccount.getInterestRate().getRate());
            accountInfo.setCreatedAt(creditAccount.getCreatedAt());
        }
        else if(common.getAccountType().equals(AccountTypes.SAVINGS)) {
            SavingAccount savingAccount = savingService.findByCommonId(common.getAccountCommonId());

            accountInfo.setCurrentAccountBalance(savingAccount.getBalance());
            accountInfo.setAccountId(savingAccount.getAccountId());
            accountInfo.setBranchName(savingAccount.getBranch().getBranchName());
            accountInfo.setInterestRate(savingAccount.getInterestRate().getRate());
            accountInfo.setCreatedAt(savingAccount.getCreatedAt());
        }
        else if(common.getAccountType().equals(AccountTypes.LOAN)) {
            LoanAccount loanAccount = loanService.findByCommonId(common.getAccountCommonId());

            accountInfo.setCurrentAccountBalance(loanAccount.getBalance());
            accountInfo.setAccountId(loanAccount.getAccountId());
            accountInfo.setBranchName(loanAccount.getBranch().getBranchName());
            accountInfo.setCreatedAt(loanAccount.getCreatedAt());
        }

        return accountInfo;
    }
}
