package com.system.transaction_service.dubbo;

import com.google.common.base.Strings;
import com.system.common_library.dto.report.TransactionReportDTO;
import com.system.common_library.dto.report.TransactionReportRequest;
import com.system.common_library.dto.request.transaction.CoreTransactionRollbackDTO;
import com.system.common_library.dto.request.transaction.CreateExternalTransactionDTO;
import com.system.common_library.dto.request.transaction.CreateInternalTransactionDTO;
import com.system.common_library.dto.response.PagedDTO;
import com.system.common_library.dto.response.account.AccountBalanceDTO;
import com.system.common_library.dto.response.account.AccountExtraCoreDTO;
import com.system.common_library.dto.response.account.AccountInfoDTO;
import com.system.common_library.dto.response.transaction.TransactionCoreNapasDTO;
import com.system.common_library.dto.transaction.TransactionDTO;
import com.system.common_library.dto.transaction.TransactionExtraDTO;
import com.system.common_library.dto.transaction.account.credit.CreateCreditDisbursementTransactionDTO;
import com.system.common_library.dto.transaction.account.credit.CreateCreditPaymentTransactionDTO;
import com.system.common_library.dto.transaction.account.credit.CreateCreditTransactionDTO;
import com.system.common_library.dto.transaction.account.credit.TransactionCreditResultDTO;
import com.system.common_library.dto.transaction.account.savings.CreateSavingsDisbursementTransactionDTO;
import com.system.common_library.dto.transaction.account.savings.CreateSavingsPaymentTransactionDTO;
import com.system.common_library.dto.transaction.account.savings.CreateSavingsTransactionDTO;
import com.system.common_library.dto.transaction.account.savings.TransactionSavingsResultDTO;
import com.system.common_library.dto.transaction.loan.CreateLoanDisbursementTransactionDTO;
import com.system.common_library.dto.transaction.loan.CreateLoanPaymentTransactionDTO;
import com.system.common_library.dto.transaction.loan.CreateLoanTransactionDTO;
import com.system.common_library.dto.transaction.loan.TransactionLoanResultDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.*;
import com.system.common_library.exception.DubboException;
import com.system.common_library.service.AccountDubboService;
import com.system.common_library.service.CustomerDubboService;
import com.system.common_library.service.TransactionDubboService;
import com.system.common_library.util.ErrorMapGenerator;
import com.system.transaction_service.client.core.AccountCoreFeignClient;
import com.system.transaction_service.client.core.TransactionCoreFeignClient;
import com.system.transaction_service.config.VaultConfig;
import com.system.transaction_service.entity.InternalTransaction;
import com.system.transaction_service.entity.Transaction;
import com.system.transaction_service.entity.TransactionDetail;
import com.system.transaction_service.entity.TransactionState;
import com.system.transaction_service.mapper.TransactionDetailMapper;
import com.system.transaction_service.repository.TransactionDetailRepository;
import com.system.transaction_service.repository.TransactionRepository;
import com.system.transaction_service.service.interfaces.PagingService;
import com.system.transaction_service.util.Constant;
import de.huxhorn.sulky.ulid.ULID;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@DubboService
@Transactional
@RequiredArgsConstructor
public class TransactionDubboServiceImpl implements TransactionDubboService {

    private static final String SORT = "id,desc";

    private static final String ROLLBACK_MESSAGE = "Rollback transaction id : ";

    private final VaultConfig vaultConfig;

    private final Validator validator;

    private final TransactionDetailMapper transactionDetailMapper;

    private final TransactionRepository transactionRepository;

    private final TransactionDetailRepository transactionDetailRepository;

    private final PagingService pagingService;

    @DubboReference
    private final CustomerDubboService customerDubboService;

    @DubboReference
    private final AccountDubboService accountDubboService;

    private final AccountCoreFeignClient accountCoreFeignClient;

    private final TransactionCoreFeignClient transactionCoreFeignClient;

    @Override
    public TransactionExtraDTO getTransactionDetail(String id) throws DubboException {

        log.info("Entering getTransactionDetail with parameters: id = {}", id);
        Optional<TransactionDetail> transaction = transactionDetailRepository.findByIdAndStatus(id, true);

        return transaction.map(transactionDetailMapper::entityToExtraDTO).orElse(null);
    }

    @Override
    public PagedDTO<TransactionDTO> getTransactionListByAccount(String account, int page, int limit) throws DubboException {

        log.info("Entering getTransactionListByAccount with parameters: account = {}, page = {}, limit = {}",
                account, page, limit);
        Pageable pageable = pagingService.getPageable(SORT, page, limit, TransactionDetail.class);
        Page<TransactionDetail> pageResult = transactionDetailRepository.findAllByAccount(account, pageable);

        return new PagedDTO<>(pageResult.map(transactionDetailMapper::entityToDTO));
    }

    @Override
    public PagedDTO<TransactionDTO> getTransactionListByCIF(String cif, int page, int limit) throws DubboException {

        log.info("Entering getTransactionListByCIF with parameters: cif = {}, page = {}, limit = {}",
                cif, page, limit);
        Pageable pageable = pagingService.getPageable(SORT, page, limit, TransactionDetail.class);
        Page<TransactionDetail> pageResult = transactionDetailRepository.findAllByCif(cif, pageable);

        return new PagedDTO<>(pageResult.map(transactionDetailMapper::entityToDTO));
    }

    @Override
    public List<TransactionReportDTO> getTransactionByFilter(TransactionReportRequest request) throws DubboException {

        log.info("Entering getTransactionByFilter with parameters: request = {}", request.toString());
        List<TransactionDetail> list = transactionDetailRepository.findAllByFilter(request);

        return list.stream().map(transactionDetailMapper::entityToReport).toList();
    }

    @Override
    public TransactionLoanResultDTO createLoanAccountDisbursement(CreateLoanDisbursementTransactionDTO create) throws DubboException {

        // Check validation
        log.info("Entering createLoanAccountDisbursement with parameters: create = {}", create.toString());
        Set<ConstraintViolation<Object>> violations = validator.validate(create);
        if (!violations.isEmpty()) {

            String errors = ErrorMapGenerator.violationToErrorMap(violations);
            log.error(errors);
            throw new DubboException(errors);
        }

        // Check customer (Customer service)
        try {

            CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(create.getCifCode());

            if (customer == null || !customer.isActive())
                throw new Exception();

            log.info("Customer info: {}", customer);
        } catch (Exception e) {

            log.error("Invalid customer");
            throw new DubboException("Invalid customer");
        }

        // Check master account validation (Core banking)
        AccountExtraCoreDTO masterAccount;
        try {

            ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
                    (vaultConfig.getAccountNumber());
            masterAccount = rsMasterAccount.getBody();

            if (masterAccount == null || !masterAccount.getIsActive())
                throw new Exception();

            log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);
        } catch (Exception e) {

            log.error("Invalid master account");
            throw new DubboException("Invalid master account");
        }

        // Check payment account validation (Account service)
        AccountInfoDTO paymentAccount;
        try {

            paymentAccount = accountDubboService.getAccountDetail(create.getPaymentAccount());

            if (paymentAccount == null || !paymentAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Payment account information with account number({}): {}", create.getPaymentAccount(), paymentAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid payment account");
            throw new DubboException("Invalid payment account");
        }

        // Check loan account validation (Account service)
        AccountInfoDTO loanAccount;
        try {

            loanAccount = accountDubboService.getAccountDetail(create.getLoanAccount());

            if (loanAccount == null || !loanAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Loan account information with account number({}): {}", create.getLoanAccount(), loanAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid loan account");
            throw new DubboException("Invalid loan account");
        }

        log.info("Transaction amount");
        BigDecimal amount = create.getAmount();
        log.info("Amount: {}", amount);
        BigDecimal loanBalance = loanAccount.getCurrentAccountBalance();
        log.info("Loan balance: {}", loanBalance);
        log.info("Current loan balance: {}", loanBalance.add(amount));
        BigDecimal paymentBalance = paymentAccount.getCurrentAccountBalance();
        log.info("Payment balance: {}", paymentBalance);
        log.info("Current payment balance: {}", paymentBalance.add(amount));

        // Create transaction
        String id = new ULID().nextULID();
        InternalTransaction transaction = InternalTransaction.builder()
                .id(id)
                .type(Type.TRANSFER)
                .cifCode(create.getCifCode())
                // Loan account (sender) information
                .senderAccountId(loanAccount.getAccountId())
                .senderAccount(loanAccount.getAccountNumber())
                .senderAccountType(loanAccount.getAccountType())
                // Payment account (receiver) information
                .receiverAccountId(paymentAccount.getAccountId())
                .receiverAccount(paymentAccount.getAccountNumber())
                .receiverAccountType(paymentAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(Initiator.CUSTOMER)
                .transactionType(TransactionType.INTERNAL)
                .method(Method.ONLINE_BANKING)
                .note(create.getNote())
                .description(create.getDescription())
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        // Create transaction detail list
        transaction.setTransactionDetailList(List.of(
                // Loan transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(loanAccount.getCustomerId())
                        .account(create.getLoanAccount())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(loanBalance)
                        .currentBalance(loanBalance.add(amount))
                        .availableBalance(loanBalance.add(amount))
                        .direction(Direction.RECEIVE)
                        .description(create.getDescription())
                        .status(true)
                        .build(),
                // Payment transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(paymentAccount.getCustomerId())
                        .account(create.getPaymentAccount())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(paymentBalance)
                        .currentBalance(paymentBalance.add(amount))
                        .availableBalance(paymentBalance.add(amount))
                        .direction(Direction.RECEIVE)
                        .description(create.getDescription())
                        .status(true)
                        .build(),
                // Master transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(masterAccount.getCustomerId())
                        .account(masterAccount.getAccountNumber())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(masterAccount.getBalance())
                        .currentBalance(masterAccount.getBalance().add(amount.negate()))
                        .availableBalance(masterAccount.getAvailableBalance().add(amount.negate()))
                        .direction(Direction.SEND)
                        .description(create.getDescription())
                        .status(true)
                        .build()));

        // Create transaction state list
        transaction.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        // Update account balance (Core banking service)
        StringBuilder sb = new StringBuilder();
        try {

            // Update payment/master account balance (Core banking)
            ResponseEntity<TransactionCoreNapasDTO> rsCoreTransaction = transactionCoreFeignClient.createInternal
                    (CreateInternalTransactionDTO.builder()
                            .senderAccountNumber(masterAccount.getAccountNumber())
                            .senderAmount(amount.negate())
                            .receiverAccountNumber(paymentAccount.getAccountNumber())
                            .receiverAmount(amount)
                            .masterAccountNumber(Constant.STRING)
                            .fee(BigDecimal.ZERO)
                            .note(create.getNote())
                            .description(create.getDescription())
                            .build());
            if (rsCoreTransaction != null) {

                log.info("Core transaction information: {}", rsCoreTransaction);
                if (rsCoreTransaction.getBody() != null)
                    sb.append(rsCoreTransaction.getBody().getReferenceCode());
            }

            // Update loan account balance (Core banking)
            ResponseEntity<TransactionCoreNapasDTO> rsLoanCoreTransaction = transactionCoreFeignClient.createExternal
                    (CreateExternalTransactionDTO.builder()
                            .accountNumber(loanAccount.getAccountNumber())
                            .amount(amount)
                            .masterAccountNumber(Constant.STRING)
                            .fee(BigDecimal.ZERO)
                            .note(create.getNote())
                            .description(create.getDescription())
                            .build());
            if (rsLoanCoreTransaction != null) {

                sb.append("|");
                log.info("Loan core transaction information: {}", rsLoanCoreTransaction);
                if (rsLoanCoreTransaction.getBody() != null)
                    sb.append(rsLoanCoreTransaction.getBody().getReferenceCode());
            }

            transaction.setCoreRollbackCode(sb.toString());
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Core banking service not available");
        }

        // Get list reference code
        int count = 0;
        String[] refs = sb.toString().split("\\|");
        log.info("List of reference code: {}", Arrays.stream(refs).toList());
        try {

            if (refs.length != 2)
                throw new Exception();

            // Update payment account balance
            AccountBalanceDTO payment = accountDubboService.updateBalance(create.getPaymentAccount(), amount);
            count++;

            // Update loan account balance
            AccountBalanceDTO loan = accountDubboService.updateBalance(create.getLoanAccount(), amount);
            count++;

            // Save transaction
            transactionRepository.save(transaction);

            return TransactionLoanResultDTO.builder()
                    .transactionId(id)
                    .balanceBankingAccount(payment.getBalance())
                    .balanceLoanAccount(loan.getBalance())
                    .build();
        } catch (Exception e) {

            if (count > 0 || refs.length > 0) {

                // Rollback core banking transaction (master/payment account)
                log.warn("Rollback core transaction (master/payment account) with code: {}", refs[0]);
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .senderAccountNumber(masterAccount.getAccountNumber())
                        .senderAmount(amount.negate())
                        .receiverAccountNumber(paymentAccount.getAccountNumber())
                        .receiverAmount(amount)
                        .masterAccountNumber(Constant.STRING)
                        .masterAmount(BigDecimal.ZERO)
                        .type(TransactionType.INTERNAL)
                        .referenceCode(refs[0])
                        .build());
            }

            if (count > 1 || refs.length > 1) {

                // Rollback core banking transaction (loan account)
                log.warn("Rollback core transaction (loan account) with code: {}", refs[1]);
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .customerAccountNumber(loanAccount.getAccountNumber())
                        .customerAmount(amount)
                        .masterAccountNumber(Constant.STRING)
                        .masterAmount(BigDecimal.ZERO)
                        .type(TransactionType.EXTERNAL)
                        .referenceCode(refs[1])
                        .build());
            }

            throw new DubboException("Update account balance fail");
        }
    }

    @Override
    public boolean rollbackLoanAccountDisbursement(String transactionId) throws DubboException {

        // Check transaction validation
        log.info("Entering rollbackLoanAccountDisbursement with parameters: transactionId = {}", transactionId);
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isPresent()) {

            log.info("Transaction is exist with transaction id: {}", transactionId);
        } else {

            log.error("Invalid transaction id");
            throw new DubboException("Invalid transaction id");
        }

        // Check payment account validation (Account service)
        AccountInfoDTO paymentAccount;
        try {

            paymentAccount = accountDubboService.getAccountDetail(transaction.get().getReceiverAccount());

            if (paymentAccount == null || !paymentAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Payment account information with account number({}): {}", transaction.get().getReceiverAccount(), paymentAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid payment account");
            throw new DubboException("Invalid payment account");
        }

        // Check loan account validation (Account service)
        AccountInfoDTO loanAccount;
        try {

            loanAccount = accountDubboService.getAccountDetail(transaction.get().getSenderAccount());

            if (loanAccount == null || !loanAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Loan account information with account number({}): {}", transaction.get().getSenderAccount(), loanAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid loan account");
            throw new DubboException("Invalid loan account");
        }

        // Check master account (Core banking)
        ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
                (vaultConfig.getAccountNumber());
        AccountExtraCoreDTO masterAccount = rsMasterAccount.getBody();
        if (masterAccount == null || !masterAccount.getIsActive()) {

            log.error("Invalid master account");
            throw new DubboException("Invalid master account");
        }
        log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);

        log.info("Transaction amount");
        BigDecimal amount = transaction.get().getAmount();
        log.info("Amount: {}", amount);
        BigDecimal loanBalance = loanAccount.getCurrentAccountBalance();
        log.info("Loan balance: {}", loanBalance);
        log.info("Current loan balance: {}", loanBalance.add(amount.negate()));
        BigDecimal paymentBalance = paymentAccount.getCurrentAccountBalance();
        log.info("Payment balance: {}", paymentBalance);
        log.info("Current payment balance: {}", paymentBalance.add(amount.negate()));

        // Create transaction
        InternalTransaction rollback = InternalTransaction.builder()
                .id(new ULID().nextULID())
                .type(Type.TRANSFER)
                .cifCode(transaction.get().getCifCode())
                // Payment account (sender) information
                .senderAccountId(paymentAccount.getAccountId())
                .senderAccount(paymentAccount.getAccountNumber())
                .senderAccountType(paymentAccount.getAccountType())
                // Loan account (receiver) information
                .receiverAccountId(loanAccount.getAccountId())
                .receiverAccount(loanAccount.getAccountNumber())
                .receiverAccountType(loanAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(Initiator.CUSTOMER)
                .transactionType(TransactionType.ROLLBACK)
                .method(Method.ONLINE_BANKING)
                .referenceCode(transactionId)
                .note(ROLLBACK_MESSAGE + transactionId)
                .description(ROLLBACK_MESSAGE + transactionId)
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        // Create transaction detail list
        rollback.setTransactionDetailList(List.of(
                // Loan transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(loanAccount.getCustomerId())
                        .account(loanAccount.getAccountNumber())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(loanBalance)
                        .currentBalance(loanBalance.add(amount.negate()))
                        .availableBalance(loanBalance.add(amount.negate()))
                        .direction(Direction.SEND)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(loanAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build(),
                // Payment transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(paymentAccount.getCustomerId())
                        .account(paymentAccount.getAccountNumber())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(paymentBalance)
                        .currentBalance(paymentBalance.add(amount.negate()))
                        .availableBalance(paymentBalance.add(amount.negate()))
                        .direction(Direction.SEND)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(paymentAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build(),
                // Master transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(masterAccount.getCustomerId())
                        .account(masterAccount.getAccountNumber())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(masterAccount.getBalance())
                        .currentBalance(masterAccount.getBalance().add(amount))
                        .availableBalance(masterAccount.getAvailableBalance().add(amount))
                        .direction(Direction.RECEIVE)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(masterAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build()));

        // Create transaction state list
        rollback.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        try {

            // Get list reference code
            String[] refs = transaction.get().getCoreRollbackCode().split("\\|");
            log.info("List of reference code: {}", Arrays.stream(refs).toList());

            // Rollback core banking transaction (master/payment account)
            log.warn("Rollback core transaction (master/payment account) with code: {}", refs[0]);
            transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                    .senderAccountNumber(masterAccount.getAccountNumber())
                    .senderAmount(amount.negate())
                    .receiverAccountNumber(paymentAccount.getAccountNumber())
                    .receiverAmount(amount)
                    .masterAccountNumber(Constant.STRING)
                    .masterAmount(BigDecimal.ZERO)
                    .type(TransactionType.INTERNAL)
                    .referenceCode(refs[0])
                    .build());

            // Rollback core banking transaction (loan account)
            log.warn("Rollback core transaction (loan account) with code: {}", refs[1]);
            transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                    .customerAccountNumber(loanAccount.getAccountNumber())
                    .customerAmount(amount)
                    .masterAccountNumber(Constant.STRING)
                    .masterAmount(BigDecimal.ZERO)
                    .type(TransactionType.EXTERNAL)
                    .referenceCode(refs[1])
                    .build());

            // Update payment account balance
            accountDubboService.updateBalance(paymentAccount.getAccountNumber(), amount.negate());

            // Update loan account balance
            accountDubboService.updateBalance(loanAccount.getAccountNumber(), amount.negate());

            // Save transaction
            transactionRepository.save(rollback);
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Rollback fail");
        }

        log.info("Rollback loan account disbursement successful");
        return true;
    }

    @Override
    public TransactionLoanResultDTO createLoanTransaction(CreateLoanTransactionDTO create) throws DubboException {

        // Check validation
        log.info("Entering createLoanTransaction with parameters: create = {}", create.toString());
        Set<ConstraintViolation<Object>> violations = validator.validate(create);
        if (!violations.isEmpty()) {

            String errors = ErrorMapGenerator.violationToErrorMap(violations);
            log.error(errors);
            throw new DubboException(errors);
        }

        // Check customer (Customer service)
        try {

            CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(create.getCifCode());

            if (customer == null || !customer.isActive())
                throw new Exception();

            log.info("Customer info: {}", customer);
        } catch (Exception e) {

            log.error("Invalid customer");
            throw new DubboException("Invalid customer");
        }

        // Check loan account validation (Account service)
        AccountInfoDTO loanAccount;
        try {

            loanAccount = accountDubboService.getAccountDetail(create.getLoanAccount());

            if (loanAccount == null || !loanAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Loan account information with account number({}): {}", create.getLoanAccount(), loanAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid loan account");
            throw new DubboException("Invalid loan account");
        }

        log.info("Transaction amount");
        BigDecimal amount = create.getAmount();
        log.info("Amount: {}", amount);
        BigDecimal loanBalance = loanAccount.getCurrentAccountBalance();
        log.info("Loan balance: {}", loanBalance);
        log.info("Current loan balance: {}", loanBalance.add(amount));

        // Create transaction
        String id = new ULID().nextULID();
        InternalTransaction transaction = InternalTransaction.builder()
                .id(id)
                .type(Type.TRANSFER)
                .cifCode(create.getCifCode())
                // Loan account (sender) information
                .senderAccountId(loanAccount.getAccountId())
                .senderAccount(loanAccount.getAccountNumber())
                .senderAccountType(loanAccount.getAccountType())
                // Loan account (receiver) information
                .receiverAccountId(loanAccount.getAccountId())
                .receiverAccount(loanAccount.getAccountNumber())
                .receiverAccountType(loanAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(Initiator.SYSTEM)
                .transactionType(TransactionType.SYSTEM)
                .method(Method.SYSTEM)
                .note(create.getNote())
                .description(create.getDescription())
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        // Create transaction detail list
        transaction.setTransactionDetailList(List.of(
                // Loan transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(loanAccount.getCustomerId())
                        .account(create.getLoanAccount())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(loanBalance)
                        .currentBalance(loanBalance.add(amount))
                        .availableBalance(loanBalance.add(amount))
                        .direction(Direction.RECEIVE)
                        .description(create.getDescription())
                        .status(true)
                        .build()));

        // Create transaction state list
        transaction.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        // Update account balance (Core banking service)
        StringBuilder sb = new StringBuilder();
        try {

            // Update loan account balance (Core banking)
            ResponseEntity<TransactionCoreNapasDTO> rsLoanCoreTransaction = transactionCoreFeignClient.createExternal
                    (CreateExternalTransactionDTO.builder()
                            .accountNumber(loanAccount.getAccountNumber())
                            .amount(amount)
                            .masterAccountNumber(Constant.STRING)
                            .fee(BigDecimal.ZERO)
                            .note(create.getNote())
                            .description(create.getDescription())
                            .build());
            if (rsLoanCoreTransaction != null) {

                log.info("Loan core transaction information: {}", rsLoanCoreTransaction);
                if (rsLoanCoreTransaction.getBody() != null)
                    sb.append(rsLoanCoreTransaction.getBody().getReferenceCode());
            }

            transaction.setCoreRollbackCode(sb.toString());
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Core banking service not available");
        }

        int count = 0;
        try {

            AccountBalanceDTO loan = accountDubboService.updateBalance(create.getLoanAccount(), amount);
            count++;

            // Save transaction
            transactionRepository.save(transaction);

            return TransactionLoanResultDTO.builder()
                    .transactionId(id)
                    .balanceLoanAccount(loan.getBalance())
                    .build();
        } catch (Exception e) {

            if (count > 0 || !Strings.isNullOrEmpty(transaction.getCoreRollbackCode())) {

                // Rollback core banking transaction (loan account)
                log.warn("Rollback core transaction (loan account) with code: {}", transaction.getCoreRollbackCode());
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .customerAccountNumber(loanAccount.getAccountNumber())
                        .customerAmount(amount)
                        .masterAccountNumber(Constant.STRING)
                        .masterAmount(BigDecimal.ZERO)
                        .type(TransactionType.EXTERNAL)
                        .referenceCode(transaction.getCoreRollbackCode())
                        .build());
            }

            throw new DubboException("Update account balance fail");
        }
    }

    @Override
    public boolean rollbackLoanTransaction(String transactionId) throws DubboException {

        // Check transaction validation
        log.info("Entering rollbackLoanTransaction with parameters: transactionId = {}", transactionId);
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isPresent()) {

            log.info("Transaction is exist with transaction id: {}", transactionId);
        } else {

            log.error("Invalid transaction id");
            throw new DubboException("Invalid transaction id");
        }

        // Check loan account validation (Account service)
        AccountInfoDTO loanAccount;
        try {

            loanAccount = accountDubboService.getAccountDetail(transaction.get().getSenderAccount());

            if (loanAccount == null || !loanAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Loan account information with account number({}): {}", transaction.get().getSenderAccount(), loanAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid loan account");
            throw new DubboException("Invalid loan account");
        }

        log.info("Transaction amount");
        BigDecimal amount = transaction.get().getAmount();
        log.info("Amount: {}", amount);
        BigDecimal loanBalance = loanAccount.getCurrentAccountBalance();
        log.info("Loan balance: {}", loanBalance);
        log.info("Current loan balance: {}", loanBalance.add(amount.negate()));

        // Create transaction
        InternalTransaction rollback = InternalTransaction.builder()
                .id(new ULID().nextULID())
                .type(Type.TRANSFER)
                .cifCode(transaction.get().getCifCode())
                // Loan account (sender) information
                .senderAccountId(loanAccount.getAccountId())
                .senderAccount(loanAccount.getAccountNumber())
                .senderAccountType(loanAccount.getAccountType())
                // Loan account (receiver) information
                .receiverAccountId(loanAccount.getAccountId())
                .receiverAccount(loanAccount.getAccountNumber())
                .receiverAccountType(loanAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(Initiator.SYSTEM)
                .transactionType(TransactionType.ROLLBACK)
                .method(Method.SYSTEM)
                .referenceCode(transactionId)
                .note(ROLLBACK_MESSAGE + transactionId)
                .description(ROLLBACK_MESSAGE + transactionId)
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        // Create transaction detail list
        rollback.setTransactionDetailList(List.of(
                // Loan transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(loanAccount.getCustomerId())
                        .account(loanAccount.getAccountNumber())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(loanBalance)
                        .currentBalance(loanBalance.add(amount.negate()))
                        .availableBalance(loanBalance.add(amount.negate()))
                        .direction(Direction.SEND)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(loanAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build()));

        // Create transaction state list
        rollback.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        try {

            // Rollback core banking transaction (loan account)
            log.warn("Rollback core transaction (loan account) with code: {}", transaction.get().getCoreRollbackCode());
            transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                    .customerAccountNumber(loanAccount.getAccountNumber())
                    .customerAmount(amount)
                    .masterAccountNumber(Constant.STRING)
                    .masterAmount(BigDecimal.ZERO)
                    .type(TransactionType.EXTERNAL)
                    .referenceCode(transaction.get().getCoreRollbackCode())
                    .build());

            // Update loan account balance
            accountDubboService.updateBalance(loanAccount.getAccountNumber(), amount.negate());

            // Save transaction
            transactionRepository.save(rollback);
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Rollback fail");
        }

        log.info("Rollback loan transaction successful");
        return true;
    }

    @Override
    public TransactionLoanResultDTO createLoanPaymentTransaction(CreateLoanPaymentTransactionDTO create) throws DubboException {

        // Check validation
        log.info("Entering createLoanPaymentTransaction with parameters: create = {}", create.toString());
        Set<ConstraintViolation<Object>> violations = validator.validate(create);
        if (!violations.isEmpty()) {

            String errors = ErrorMapGenerator.violationToErrorMap(violations);
            log.error(errors);
            throw new DubboException(errors);
        }

        // Check customer (Customer service)
        try {

            CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(create.getCifCode());

            if (customer == null || !customer.isActive())
                throw new Exception();

            log.info("Customer info: {}", customer);
        } catch (Exception e) {

            log.error("Invalid customer");
            throw new DubboException("Invalid customer");
        }

        // Check master account validation (Core banking)
        AccountExtraCoreDTO masterAccount;
        try {

            ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
                    (vaultConfig.getAccountNumber());
            masterAccount = rsMasterAccount.getBody();

            if (masterAccount == null || !masterAccount.getIsActive())
                throw new Exception();

            log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);
        } catch (Exception e) {

            log.error("Invalid master account");
            throw new DubboException("Invalid master account");
        }

        // Check payment account validation (Account service)
        AccountInfoDTO paymentAccount = AccountInfoDTO.builder().build();
        if (!create.isCashPayment()) {

            try {

                paymentAccount = accountDubboService.getAccountDetail(create.getPaymentAccount());

                if (paymentAccount == null || !paymentAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                    throw new Exception();

                log.info("Payment account information with account number({}): {}", create.getPaymentAccount(), paymentAccount);
            } catch (Exception e) {

                if (e instanceof DubboException) {

                    log.error(e.getMessage());
                }

                log.error("Invalid payment account");
                throw new DubboException("Invalid payment account");
            }
        }

        // Check loan account validation (Account service)
        AccountInfoDTO loanAccount;
        try {

            loanAccount = accountDubboService.getAccountDetail(create.getLoanAccount());

            if (loanAccount == null || !loanAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Loan account information with account number({}): {}", create.getLoanAccount(), loanAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid loan account");
            throw new DubboException("Invalid loan account");
        }

        if (!create.isCashPayment() && create.getAmount().compareTo(paymentAccount.getCurrentAccountBalance()) > 0) {

            log.info("Payment amount exceeds payment balance");
            throw new DubboException("Payment amount exceeds payment balance");
        }

        if (create.getAmount().compareTo(loanAccount.getCurrentAccountBalance()) > 0) {

            log.info("Payment amount exceeds debit balance");
            throw new DubboException("Payment amount exceeds debit balance");
        }

        log.info("Transaction amount");
        BigDecimal amount = create.getAmount();
        log.info("Amount: {}", amount);
        BigDecimal loanBalance = loanAccount.getCurrentAccountBalance();
        log.info("Loan balance: {}", loanBalance);
        log.info("Current loan balance: {}", loanBalance.add(amount.negate()));
        BigDecimal paymentBalance = paymentAccount.getCurrentAccountBalance();
        if (!create.isCashPayment()) {

            log.info("Payment balance: {}", paymentBalance);
            log.info("Current payment balance: {}", paymentBalance.add(amount.negate()));
        }

        // Create transaction
        String id = new ULID().nextULID();
        InternalTransaction transaction = InternalTransaction.builder()
                .id(id)
                .type(Type.TRANSFER)
                .cifCode(create.getCifCode())
                // Loan account (sender) information
                .senderAccountId(loanAccount.getAccountId())
                .senderAccount(loanAccount.getAccountNumber())
                .senderAccountType(loanAccount.getAccountType())
                // Master account (receiver) information
                .receiverAccountId(masterAccount.getAccountId())
                .receiverAccount(masterAccount.getAccountNumber())
                .receiverAccountType(masterAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(create.isCashPayment() ? Initiator.EMPLOYEE : Initiator.CUSTOMER)
                .transactionType(TransactionType.INTERNAL)
                .method(create.isCashPayment() ? Method.IN_BRANCH : Method.ONLINE_BANKING)
                .note(create.getNote())
                .description(create.getDescription())
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        // Create transaction detail list
        List<TransactionDetail> details = new ArrayList<>(List.of(
                // Loan transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(loanAccount.getCustomerId())
                        .account(create.getLoanAccount())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(loanBalance)
                        .currentBalance(loanBalance.add(amount.negate()))
                        .availableBalance(loanBalance.add(amount.negate()))
                        .direction(Direction.SEND)
                        .description(create.getDescription())
                        .status(true)
                        .build(),
                // Master transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(masterAccount.getCustomerId())
                        .account(masterAccount.getAccountNumber())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(masterAccount.getBalance())
                        .currentBalance(masterAccount.getBalance().add(amount))
                        .availableBalance(masterAccount.getAvailableBalance().add(amount))
                        .direction(Direction.RECEIVE)
                        .description(create.getDescription())
                        .status(true)
                        .build()
        ));

        if (!create.isCashPayment()) {

            details.add(
                    // Payment transaction
                    TransactionDetail.builder()
                            .id(new ULID().nextULID())
                            .transaction(transaction)
                            .customerId(paymentAccount.getCustomerId())
                            .account(create.getPaymentAccount())
                            .amount(amount.negate())
                            .fee(BigDecimal.ZERO)
                            .netAmount(amount.negate())
                            .previousBalance(paymentBalance)
                            .currentBalance(paymentBalance.add(amount.negate()))
                            .availableBalance(paymentBalance.add(amount.negate()))
                            .direction(Direction.SEND)
                            .description(create.getDescription())
                            .status(true)
                            .build());
        }

        // Set transaction detail list
        transaction.setTransactionDetailList(details);

        // Create transaction state list
        transaction.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        // Update account balance (Core banking service)
        StringBuilder sb = new StringBuilder();
        try {

            if (!create.isCashPayment()) {

                // Update payment/master account balance (Core banking)
                ResponseEntity<TransactionCoreNapasDTO> rsCoreTransaction = transactionCoreFeignClient.createInternal
                        (CreateInternalTransactionDTO.builder()
                                .senderAccountNumber(paymentAccount.getAccountNumber())
                                .senderAmount(amount.negate())
                                .receiverAccountNumber(masterAccount.getAccountNumber())
                                .receiverAmount(amount)
                                .masterAccountNumber(Constant.STRING)
                                .fee(BigDecimal.ZERO)
                                .note(create.getNote())
                                .description(create.getDescription())
                                .build());
                if (rsCoreTransaction != null) {

                    log.info("Core transaction information: {}", rsCoreTransaction);
                    if (rsCoreTransaction.getBody() != null)
                        sb.append(rsCoreTransaction.getBody().getReferenceCode());
                }

                // Update loan account balance (Core banking)
                ResponseEntity<TransactionCoreNapasDTO> rsLoanCoreTransaction = transactionCoreFeignClient.createExternal
                        (CreateExternalTransactionDTO.builder()
                                .accountNumber(loanAccount.getAccountNumber())
                                .amount(amount.negate())
                                .masterAccountNumber(Constant.STRING)
                                .fee(BigDecimal.ZERO)
                                .note(create.getNote())
                                .description(create.getDescription())
                                .build());
                if (rsLoanCoreTransaction != null) {

                    sb.append("|");
                    log.info("Loan core transaction information: {}", rsLoanCoreTransaction);
                    if (rsLoanCoreTransaction.getBody() != null)
                        sb.append(rsLoanCoreTransaction.getBody().getReferenceCode());
                }
            } else {

                // Update loan/master account balance (Core banking)
                ResponseEntity<TransactionCoreNapasDTO> rsCoreTransaction = transactionCoreFeignClient.createInternal
                        (CreateInternalTransactionDTO.builder()
                                .senderAccountNumber(loanAccount.getAccountNumber())
                                .senderAmount(amount.negate())
                                .receiverAccountNumber(masterAccount.getAccountNumber())
                                .receiverAmount(amount)
                                .masterAccountNumber(Constant.STRING)
                                .fee(BigDecimal.ZERO)
                                .note(create.getNote())
                                .description(create.getDescription())
                                .build());
                if (rsCoreTransaction != null) {

                    log.info("Core transaction information: {}", rsCoreTransaction);
                    if (rsCoreTransaction.getBody() != null)
                        sb.append(rsCoreTransaction.getBody().getReferenceCode());
                }
            }

            transaction.setCoreRollbackCode(sb.toString());
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Core banking service not available");
        }

        // Get list reference code
        int count = 0;
        String[] refs = sb.toString().split("\\|");
        log.info("List of reference code: {}", Arrays.stream(refs).toList());

        try {

            if (refs.length != 2 && !create.isCashPayment())
                throw new Exception();

            TransactionLoanResultDTO result = TransactionLoanResultDTO.builder()
                    .transactionId(id)
                    .build();

            if (!create.isCashPayment()) {

                // Update payment account balance
                AccountBalanceDTO payment = accountDubboService.updateBalance(create.getPaymentAccount(), amount.negate());
                count++;
                result.setBalanceBankingAccount(payment.getBalance());
            }

            // Update loan account balance
            AccountBalanceDTO loan = accountDubboService.updateBalance(create.getLoanAccount(), amount.negate());
            count++;
            result.setBalanceLoanAccount(loan.getBalance());

            // Save transaction
            transactionRepository.save(transaction);

            return result;
        } catch (Exception e) {

            if (!create.isCashPayment()) {

                if (count > 0 || refs.length > 0) {

                    // Rollback core banking transaction (master/payment account)
                    log.warn("Rollback core transaction (master/payment account) with code: {}", refs[0]);
                    transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                            .senderAccountNumber(paymentAccount.getAccountNumber())
                            .senderAmount(amount.negate())
                            .receiverAccountNumber(masterAccount.getAccountNumber())
                            .receiverAmount(amount)
                            .masterAccountNumber(Constant.STRING)
                            .masterAmount(BigDecimal.ZERO)
                            .type(TransactionType.INTERNAL)
                            .referenceCode(refs[0])
                            .build());
                }

                if (count > 1 || refs.length > 1) {

                    // Rollback core banking transaction (loan account)
                    log.warn("Rollback core transaction (loan account) with code: {}", refs[1]);
                    transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                            .customerAccountNumber(loanAccount.getAccountNumber())
                            .customerAmount(amount.negate())
                            .masterAccountNumber(Constant.STRING)
                            .masterAmount(BigDecimal.ZERO)
                            .type(TransactionType.EXTERNAL)
                            .referenceCode(refs[1])
                            .build());
                }
            } else {

                if (count > 0 || !Strings.isNullOrEmpty(refs[0])) {

                    // Rollback core banking transaction (loan/master account)
                    log.warn("Rollback core transaction (loan/master account) with code: {}", refs[0]);
                    transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                            .senderAccountNumber(loanAccount.getAccountNumber())
                            .senderAmount(amount.negate())
                            .receiverAccountNumber(masterAccount.getAccountNumber())
                            .receiverAmount(amount)
                            .masterAccountNumber(Constant.STRING)
                            .masterAmount(BigDecimal.ZERO)
                            .type(TransactionType.INTERNAL)
                            .referenceCode(refs[0])
                            .build());
                }
            }

            throw new DubboException("Update account balance fail");
        }
    }

    @Override
    public boolean rollbackLoanPaymentTransaction(String transactionId) throws DubboException {

        // Check transaction validation
        log.info("Entering rollbackLoanPaymentTransaction with parameters: transactionId = {}", transactionId);
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isPresent()) {

            log.info("Transaction is exist with transaction id: {}", transactionId);
        } else {

            log.error("Invalid transaction id");
            throw new DubboException("Invalid transaction id");
        }

        // Check master account (Core banking)
        ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
                (vaultConfig.getAccountNumber());
        AccountExtraCoreDTO masterAccount = rsMasterAccount.getBody();
        if (masterAccount == null || !masterAccount.getIsActive()) {

            log.error("Invalid master account");
            throw new DubboException("Invalid master account");
        }
        log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);

        // Check payment account validation (Account service)
        AccountInfoDTO paymentAccount = AccountInfoDTO.builder().build();
        if (transaction.get().getMethod().equals(Method.ONLINE_BANKING)) {

            try {

                String paymentAccountNum = transaction.get().getTransactionDetailList()
                        .stream().filter(t -> t.getDirection().equals(Direction.SEND)
                                && !t.getAccount().equals(transaction.get().getSenderAccount())).findFirst()
                        .orElseThrow().getAccount();
                paymentAccount = accountDubboService.getAccountDetail(paymentAccountNum);

                if (paymentAccount == null || !paymentAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                    throw new Exception();

                log.info("Payment account information with account number({}): {}", paymentAccountNum, paymentAccount);
            } catch (Exception e) {

                if (e instanceof DubboException) {

                    log.error(e.getMessage());
                }

                log.error("Invalid payment account");
                throw new DubboException("Invalid payment account");
            }
        }

        // Check loan account validation (Account service)
        AccountInfoDTO loanAccount;
        try {

            loanAccount = accountDubboService.getAccountDetail(transaction.get().getSenderAccount());

            if (loanAccount == null || !loanAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Loan account information with account number({}): {}",
                    transaction.get().getSenderAccount(), loanAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid loan account");
            throw new DubboException("Invalid loan account");
        }

        log.info("Transaction amount");
        BigDecimal amount = transaction.get().getAmount();
        log.info("Amount: {}", amount);
        BigDecimal loanBalance = loanAccount.getCurrentAccountBalance();
        log.info("Loan balance: {}", loanBalance);
        log.info("Current loan balance: {}", loanBalance.add(amount));
        BigDecimal paymentBalance = paymentAccount.getCurrentAccountBalance();
        if (transaction.get().getMethod().equals(Method.ONLINE_BANKING)) {

            log.info("Payment balance: {}", paymentBalance);
            log.info("Current payment balance: {}", paymentBalance.add(amount));
        }

        // Create transaction
        InternalTransaction rollback = InternalTransaction.builder()
                .id(new ULID().nextULID())
                .type(Type.TRANSFER)
                .cifCode(transaction.get().getCifCode())
                // Master account (sender) information
                .senderAccountId(masterAccount.getAccountId())
                .senderAccount(masterAccount.getAccountNumber())
                .senderAccountType(masterAccount.getAccountType())
                // Loan account (receiver) information
                .receiverAccountId(loanAccount.getAccountId())
                .receiverAccount(loanAccount.getAccountNumber())
                .receiverAccountType(loanAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(Initiator.CUSTOMER)
                .transactionType(TransactionType.ROLLBACK)
                .method(Method.ONLINE_BANKING)
                .referenceCode(transactionId)
                .note(ROLLBACK_MESSAGE + transactionId)
                .description(ROLLBACK_MESSAGE + transactionId)
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        // Create transaction detail list
        List<TransactionDetail> details = new ArrayList<>(List.of(
                // Loan transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(loanAccount.getCustomerId())
                        .account(loanAccount.getAccountNumber())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(loanBalance)
                        .currentBalance(loanBalance.add(amount))
                        .availableBalance(loanBalance.add(amount))
                        .direction(Direction.SEND)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(loanAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build(),
                // Master transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(masterAccount.getCustomerId())
                        .account(masterAccount.getAccountNumber())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(masterAccount.getBalance())
                        .currentBalance(masterAccount.getBalance().add(amount.negate()))
                        .availableBalance(masterAccount.getAvailableBalance().add(amount.negate()))
                        .direction(Direction.RECEIVE)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(masterAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build()
        ));

        if (transaction.get().getMethod().equals(Method.ONLINE_BANKING)) {

            AccountInfoDTO finalPaymentAccount = paymentAccount;
            details.add(
                    // Payment transaction
                    TransactionDetail.builder()
                            .id(new ULID().nextULID())
                            .transaction(rollback)
                            .customerId(paymentAccount.getCustomerId())
                            .account(paymentAccount.getAccountNumber())
                            .amount(amount)
                            .fee(BigDecimal.ZERO)
                            .netAmount(amount)
                            .previousBalance(paymentBalance)
                            .currentBalance(paymentBalance.add(amount))
                            .availableBalance(paymentBalance.add(amount))
                            .direction(Direction.SEND)
                            .description(ROLLBACK_MESSAGE + transaction.get()
                                    .getTransactionDetailList().stream()
                                    .filter(e -> e.getAccount().equals(finalPaymentAccount.getAccountNumber()))
                                    .findFirst().orElse(new TransactionDetail()).getId())
                            .status(true)
                            .build());
        }

        // Set transaction detail list
        rollback.setTransactionDetailList(details);

        // Create transaction state list
        rollback.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        try {

            // Get list reference code
            String[] refs = transaction.get().getCoreRollbackCode().split("\\|");
            log.info("List of reference code: {}", Arrays.stream(refs).toList());

            if (transaction.get().getMethod().equals(Method.ONLINE_BANKING)) {

                // Rollback core banking transaction (master/payment account)
                log.warn("Rollback core transaction (master/payment account) with code: {}", refs[0]);
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .senderAccountNumber(paymentAccount.getAccountNumber())
                        .senderAmount(amount.negate())
                        .receiverAccountNumber(masterAccount.getAccountNumber())
                        .receiverAmount(amount)
                        .masterAccountNumber(Constant.STRING)
                        .masterAmount(BigDecimal.ZERO)
                        .type(TransactionType.INTERNAL)
                        .referenceCode(refs[0])
                        .build());

                // Rollback core banking transaction (loan account)
                log.warn("Rollback core transaction (loan account) with code: {}", refs[1]);
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .customerAccountNumber(loanAccount.getAccountNumber())
                        .customerAmount(amount.negate())
                        .masterAccountNumber(Constant.STRING)
                        .masterAmount(BigDecimal.ZERO)
                        .type(TransactionType.EXTERNAL)
                        .referenceCode(refs[1])
                        .build());

                // Update payment account balance
                accountDubboService.updateBalance(paymentAccount.getAccountNumber(), amount);
            } else {

                // Rollback core banking transaction (loan/master account)
                log.warn("Rollback core transaction (loan/master account) with code: {}", refs[0]);
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .senderAccountNumber(loanAccount.getAccountNumber())
                        .senderAmount(amount.negate())
                        .receiverAccountNumber(masterAccount.getAccountNumber())
                        .receiverAmount(amount)
                        .masterAccountNumber(Constant.STRING)
                        .masterAmount(BigDecimal.ZERO)
                        .type(TransactionType.INTERNAL)
                        .referenceCode(refs[0])
                        .build());
            }

            // Update loan account balance
            accountDubboService.updateBalance(loanAccount.getAccountNumber(), amount);

            // Save transaction
            transactionRepository.save(rollback);
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Rollback fail");
        }

        log.info("Rollback loan payment transaction successful");
        return true;
    }

    @Override
    public TransactionSavingsResultDTO createSavingsPaymentTransaction(CreateSavingsDisbursementTransactionDTO create)
            throws DubboException {

        // Check validation
        log.info("Entering createSavingsPaymentTransaction with parameters: create = {}", create.toString());
        Set<ConstraintViolation<Object>> violations = validator.validate(create);
        if (!violations.isEmpty()) {

            String errors = ErrorMapGenerator.violationToErrorMap(violations);
            log.error(errors);
            throw new DubboException(errors);
        }

        // Check customer (Customer service)
        try {

            CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(create.getCifCode());

            if (customer == null || !customer.isActive())
                throw new Exception();

            log.info("Customer info: {}", customer);
        } catch (Exception e) {

            log.error("Invalid customer");
            throw new DubboException("Invalid customer");
        }

        // Check payment account validation (Account service)
        AccountInfoDTO paymentAccount;
        try {

            paymentAccount = accountDubboService.getAccountDetail(create.getPaymentAccount());

            if (paymentAccount == null || !paymentAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Payment account information with account number({}): {}", create.getPaymentAccount(), paymentAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid payment account");
            throw new DubboException("Invalid payment account");
        }

        // Check savings account validation (Account service)
        AccountInfoDTO savingsAccount;
        try {

            savingsAccount = accountDubboService.getAccountDetail(create.getSavingAccount());

            if (savingsAccount == null || !savingsAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Savings account information with account number({}): {}", create.getSavingAccount(), savingsAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid savings account");
            throw new DubboException("Invalid savings account");
        }

        log.info("Transaction amount");
        BigDecimal amount = create.getAmount();
        log.info("Amount: {}", amount);
        BigDecimal paymentBalance = paymentAccount.getCurrentAccountBalance();
        log.info("Payment balance: {}", paymentBalance);
        log.info("Current payment balance: {}", paymentBalance.add(amount.negate()));
        BigDecimal savingsBalance = savingsAccount.getCurrentAccountBalance();
        log.info("Savings balance: {}", savingsBalance);
        log.info("Current savings balance: {}", savingsBalance.add(amount));

        if (paymentBalance.compareTo(create.getAmount()) < 0) {

            log.info("The amount of savings deposit exceeds the balance of the checking account.");
            throw new DubboException("The amount of savings deposit exceeds the balance of the checking account.");
        }

        // Create transaction
        String id = new ULID().nextULID();
        InternalTransaction transaction = InternalTransaction.builder()
                .id(id)
                .type(Type.TRANSFER)
                .cifCode(create.getCifCode())
                // Payment account (sender) information
                .senderAccountId(paymentAccount.getAccountId())
                .senderAccount(paymentAccount.getAccountNumber())
                .senderAccountType(paymentAccount.getAccountType())
                // Savings account (receiver) information
                .receiverAccountId(savingsAccount.getAccountId())
                .receiverAccount(savingsAccount.getAccountNumber())
                .receiverAccountType(savingsAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(Initiator.CUSTOMER)
                .transactionType(TransactionType.INTERNAL)
                .method(Method.ONLINE_BANKING)
                .note(create.getNote())
                .description(create.getDescription())
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        // Create transaction detail list
        transaction.setTransactionDetailList(List.of(
                // Savings transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(savingsAccount.getCustomerId())
                        .account(create.getSavingAccount())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(savingsBalance)
                        .currentBalance(savingsBalance.add(amount))
                        .availableBalance(savingsBalance.add(amount))
                        .direction(Direction.RECEIVE)
                        .description(create.getDescription())
                        .status(true)
                        .build(),
                // Payment transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(paymentAccount.getCustomerId())
                        .account(create.getPaymentAccount())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(paymentBalance)
                        .currentBalance(paymentBalance.add(amount.negate()))
                        .availableBalance(paymentBalance.add(amount.negate()))
                        .direction(Direction.SEND)
                        .description(create.getDescription())
                        .status(true)
                        .build()));

        // Create transaction state list
        transaction.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        // Update account balance (Core banking service)
        try {

            // Update payment/savings account balance (Core banking)
            ResponseEntity<TransactionCoreNapasDTO> rsCoreTransaction = transactionCoreFeignClient.createInternal
                    (CreateInternalTransactionDTO.builder()
                            .senderAccountNumber(paymentAccount.getAccountNumber())
                            .senderAmount(amount.negate())
                            .receiverAccountNumber(savingsAccount.getAccountNumber())
                            .receiverAmount(amount)
                            .masterAccountNumber(Constant.STRING)
                            .fee(BigDecimal.ZERO)
                            .note(create.getNote())
                            .description(create.getDescription())
                            .build());
            if (rsCoreTransaction != null) {

                log.info("Core transaction information: {}", rsCoreTransaction);
                if (rsCoreTransaction.getBody() != null)
                    transaction.setCoreRollbackCode(rsCoreTransaction.getBody().getReferenceCode());
            }

        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Core banking service not available");
        }

        // Update account balance (Account service)
        int count = 0;
        try {

            // Update payment account balance
            AccountBalanceDTO payment = accountDubboService.updateBalance(create.getPaymentAccount(), amount.negate());
            count++;

            // Update payment account balance
            AccountBalanceDTO savings = accountDubboService.updateBalance(create.getSavingAccount(), amount);
            count++;

            // Save transaction
            transactionRepository.save(transaction);

            return TransactionSavingsResultDTO.builder()
                    .transactionId(id)
                    .balanceBankingAccount(payment.getBalance())
                    .balanceSavingsAccount(savings.getBalance())
                    .build();
        } catch (Exception e) {

            if (count > 0) {

                // Rollback core banking transaction (savings/payment account)
                log.warn("Rollback core transaction (savings/payment account) with code: {}", transaction.getCoreRollbackCode());
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .senderAccountNumber(paymentAccount.getAccountNumber())
                        .senderAmount(amount.negate())
                        .receiverAccountNumber(savingsAccount.getAccountNumber())
                        .receiverAmount(amount)
                        .masterAccountNumber(Constant.STRING)
                        .masterAmount(BigDecimal.ZERO)
                        .type(TransactionType.INTERNAL)
                        .referenceCode(transaction.getCoreRollbackCode())
                        .build());
            }

            throw new DubboException("Update account balance fail");
        }
    }

    @Override
    public boolean rollbackSavingsPaymentTransaction(String transactionId) throws DubboException {

        // Check transaction validation
        log.info("Entering rollbackSavingsPaymentTransaction with parameters: transactionId = {}", transactionId);
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isPresent()) {

            log.info("Transaction is exist with transaction id: {}", transactionId);
        } else {

            log.error("Invalid transaction id");
            throw new DubboException("Invalid transaction id");
        }

        // Check payment account validation (Account service)
        AccountInfoDTO paymentAccount;
        try {

            paymentAccount = accountDubboService.getAccountDetail(transaction.get().getSenderAccount());

            if (paymentAccount == null || !paymentAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Payment account information with account number({}): {}",
                    transaction.get().getSenderAccount(), paymentAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid payment account");
            throw new DubboException("Invalid payment account");
        }

        // Check savings account validation (Account service)
        AccountInfoDTO savingsAccount;
        try {

            savingsAccount = accountDubboService.getAccountDetail(transaction.get().getReceiverAccount());

            if (savingsAccount == null || !savingsAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Savings account information with account number({}): {}",
                    transaction.get().getReceiverAccount(), savingsAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid savings account");
            throw new DubboException("Invalid savings account");
        }

        log.info("Transaction amount");
        BigDecimal amount = transaction.get().getAmount();
        log.info("Amount: {}", amount);
        BigDecimal paymentBalance = paymentAccount.getCurrentAccountBalance();
        log.info("Payment balance: {}", paymentBalance);
        log.info("Current payment balance: {}", paymentBalance.add(amount));
        BigDecimal savingsBalance = savingsAccount.getCurrentAccountBalance();
        log.info("Savings balance: {}", savingsBalance);
        log.info("Current savings balance: {}", savingsBalance.add(amount.negate()));

        // Create transaction
        InternalTransaction rollback = InternalTransaction.builder()
                .id(new ULID().nextULID())
                .type(Type.TRANSFER)
                .cifCode(transaction.get().getCifCode())
                // Savings account (sender) information
                .senderAccountId(savingsAccount.getAccountId())
                .senderAccount(savingsAccount.getAccountNumber())
                .senderAccountType(savingsAccount.getAccountType())
                // Payment account (receiver) information
                .receiverAccountId(paymentAccount.getAccountId())
                .receiverAccount(paymentAccount.getAccountNumber())
                .receiverAccountType(paymentAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(Initiator.CUSTOMER)
                .transactionType(TransactionType.ROLLBACK)
                .method(Method.ONLINE_BANKING)
                .referenceCode(transactionId)
                .note(ROLLBACK_MESSAGE + transactionId)
                .description(ROLLBACK_MESSAGE + transactionId)
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        // Create transaction detail list
        rollback.setTransactionDetailList(List.of(
                // Savings transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(savingsAccount.getCustomerId())
                        .account(savingsAccount.getAccountNumber())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(savingsBalance)
                        .currentBalance(savingsBalance.add(amount.negate()))
                        .availableBalance(savingsBalance.add(amount.negate()))
                        .direction(Direction.SEND)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(savingsAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build(),
                // Payment transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(paymentAccount.getCustomerId())
                        .account(paymentAccount.getAccountNumber())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(paymentBalance)
                        .currentBalance(paymentBalance.add(amount))
                        .availableBalance(paymentBalance.add(amount))
                        .direction(Direction.SEND)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(paymentAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build()));

        // Create transaction state list
        rollback.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        try {

            // Rollback core banking transaction (savings/payment account)
            log.warn("Rollback core transaction (savings/payment account) with code: {}",
                    transaction.get().getCoreRollbackCode());
            transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                    .senderAccountNumber(paymentAccount.getAccountNumber())
                    .senderAmount(amount.negate())
                    .receiverAccountNumber(savingsAccount.getAccountNumber())
                    .receiverAmount(amount)
                    .masterAccountNumber(Constant.STRING)
                    .masterAmount(BigDecimal.ZERO)
                    .type(TransactionType.INTERNAL)
                    .referenceCode(transaction.get().getCoreRollbackCode())
                    .build());

            // Update payment account balance
            accountDubboService.updateBalance(paymentAccount.getAccountNumber(), amount);

            // Update savings account balance
            accountDubboService.updateBalance(savingsAccount.getAccountNumber(), amount.negate());

            // Save transaction
            transactionRepository.save(rollback);
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Rollback fail");
        }

        log.info("Rollback savings payment transaction successful");
        return true;
    }

    @Override
    public TransactionSavingsResultDTO createSavingsTransaction(CreateSavingsTransactionDTO create) throws DubboException {

        // Check validation
        log.info("Entering createSavingsTransaction with parameters: create = {}", create.toString());
        Set<ConstraintViolation<Object>> violations = validator.validate(create);
        if (!violations.isEmpty()) {

            String errors = ErrorMapGenerator.violationToErrorMap(violations);
            log.error(errors);
            throw new DubboException(errors);
        }

        // Check customer (Customer service)
        try {

            CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(create.getCifCode());

            if (customer == null || !customer.isActive())
                throw new Exception();

            log.info("Customer info: {}", customer);
        } catch (Exception e) {

            log.error("Invalid customer");
            throw new DubboException("Invalid customer");
        }

        // Check master account validation (Core banking)
        AccountExtraCoreDTO masterAccount;
        try {

            ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
                    (vaultConfig.getAccountNumber());
            masterAccount = rsMasterAccount.getBody();

            if (masterAccount == null || !masterAccount.getIsActive())
                throw new Exception();

            log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);
        } catch (Exception e) {

            log.error("Invalid master account");
            throw new DubboException("Invalid master account");
        }

        // Check savings account validation (Account service)
        AccountInfoDTO savingsAccount;
        try {

            savingsAccount = accountDubboService.getAccountDetail(create.getSavingAccount());

            if (savingsAccount == null || !savingsAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Savings account information with account number({}): {}", create.getSavingAccount(), savingsAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid savings account");
            throw new DubboException("Invalid savings account");
        }

        log.info("Transaction amount");
        BigDecimal amount = create.getAmount();
        log.info("Amount: {}", amount);
        BigDecimal savingsBalance = savingsAccount.getCurrentAccountBalance();
        log.info("Savings balance: {}", savingsBalance);
        log.info("Current savings balance: {}", savingsBalance.add(amount));

        // Create transaction
        String id = new ULID().nextULID();
        InternalTransaction transaction = InternalTransaction.builder()
                .id(id)
                .type(Type.TRANSFER)
                .cifCode(create.getCifCode())
                // Master account (sender) information
                .senderAccountId(masterAccount.getAccountId())
                .senderAccount(masterAccount.getAccountNumber())
                .senderAccountType(masterAccount.getAccountType())
                // Savings account (receiver) information
                .receiverAccountId(savingsAccount.getAccountId())
                .receiverAccount(savingsAccount.getAccountNumber())
                .receiverAccountType(savingsAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(Initiator.SYSTEM)
                .transactionType(TransactionType.SYSTEM)
                .method(Method.SYSTEM)
                .note(create.getNote())
                .description(create.getDescription())
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        // Create transaction detail list
        transaction.setTransactionDetailList(List.of(
                // Master transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(masterAccount.getCustomerId())
                        .account(masterAccount.getAccountNumber())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(masterAccount.getBalance())
                        .currentBalance(masterAccount.getBalance().add(amount.negate()))
                        .availableBalance(masterAccount.getAvailableBalance().add(amount.negate()))
                        .direction(Direction.SEND)
                        .description(create.getDescription())
                        .status(true)
                        .build(),
                // Savings transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(savingsAccount.getCustomerId())
                        .account(create.getSavingAccount())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(savingsBalance)
                        .currentBalance(savingsBalance.add(amount))
                        .availableBalance(savingsBalance.add(amount))
                        .direction(Direction.RECEIVE)
                        .description(create.getDescription())
                        .status(true)
                        .build()));

        // Create transaction state list
        transaction.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        // Update account balance (Core banking service)
        try {

            // Update master/savings account balance (Core banking)
            ResponseEntity<TransactionCoreNapasDTO> rsCoreTransaction = transactionCoreFeignClient.createInternal
                    (CreateInternalTransactionDTO.builder()
                            .senderAccountNumber(masterAccount.getAccountNumber())
                            .senderAmount(amount.negate())
                            .receiverAccountNumber(savingsAccount.getAccountNumber())
                            .receiverAmount(amount)
                            .masterAccountNumber(Constant.STRING)
                            .fee(BigDecimal.ZERO)
                            .note(create.getNote())
                            .description(create.getDescription())
                            .build());
            if (rsCoreTransaction != null) {

                log.info("Core transaction information: {}", rsCoreTransaction);
                if (rsCoreTransaction.getBody() != null)
                    transaction.setCoreRollbackCode(rsCoreTransaction.getBody().getReferenceCode());
            }

        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Core banking service not available");
        }

        int count = 0;
        try {

            AccountBalanceDTO savings = accountDubboService.updateBalance(create.getSavingAccount(), amount);
            count++;

            // Save transaction
            transactionRepository.save(transaction);

            return TransactionSavingsResultDTO.builder()
                    .transactionId(id)
                    .balanceSavingsAccount(savings.getBalance())
                    .build();
        } catch (Exception e) {

            if (count > 0 || !Strings.isNullOrEmpty(transaction.getCoreRollbackCode())) {

                // Rollback core banking transaction (master/savings account)
                log.warn("Rollback core transaction (master/savings account) with code: {}",
                        transaction.getCoreRollbackCode());
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .senderAccountNumber(masterAccount.getAccountNumber())
                        .senderAmount(amount.negate())
                        .receiverAccountNumber(savingsAccount.getAccountNumber())
                        .receiverAmount(amount)
                        .masterAccountNumber(Constant.STRING)
                        .masterAmount(BigDecimal.ZERO)
                        .type(TransactionType.INTERNAL)
                        .referenceCode(transaction.getCoreRollbackCode())
                        .build());
            }

            throw new DubboException("Update account balance fail");
        }
    }

    @Override
    public boolean rollbackSavingsTransaction(String transactionId) throws DubboException {

        // Check transaction validation
        log.info("Entering rollbackSavingsTransaction with parameters: transactionId = {}", transactionId);
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isPresent()) {

            log.info("Transaction is exist with transaction id: {}", transactionId);
        } else {

            log.error("Invalid transaction id");
            throw new DubboException("Invalid transaction id");
        }

        // Check master account validation (Core banking)
        AccountExtraCoreDTO masterAccount;
        try {

            ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
                    (vaultConfig.getAccountNumber());
            masterAccount = rsMasterAccount.getBody();

            if (masterAccount == null || !masterAccount.getIsActive())
                throw new Exception();

            log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);
        } catch (Exception e) {

            log.error("Invalid master account");
            throw new DubboException("Invalid master account");
        }

        // Check savings account validation (Account service)
        AccountInfoDTO savingsAccount;
        try {

            savingsAccount = accountDubboService.getAccountDetail(transaction.get().getReceiverAccount());

            if (savingsAccount == null || !savingsAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Savings account information with account number({}): {}",
                    transaction.get().getReceiverAccount(), savingsAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid savings account");
            throw new DubboException("Invalid savings account");
        }

        log.info("Transaction amount");
        BigDecimal amount = transaction.get().getAmount();
        log.info("Amount: {}", amount);
        BigDecimal savingsBalance = savingsAccount.getCurrentAccountBalance();
        log.info("Savings balance: {}", savingsBalance);
        log.info("Current savings balance: {}", savingsBalance.add(amount.negate()));

        // Create transaction
        InternalTransaction rollback = InternalTransaction.builder()
                .id(new ULID().nextULID())
                .type(Type.TRANSFER)
                .cifCode(transaction.get().getCifCode())
                // Savings account (sender) information
                .senderAccountId(savingsAccount.getAccountId())
                .senderAccount(savingsAccount.getAccountNumber())
                .senderAccountType(savingsAccount.getAccountType())
                // Master account (receiver) information
                .receiverAccountId(masterAccount.getAccountId())
                .receiverAccount(masterAccount.getAccountNumber())
                .receiverAccountType(masterAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(Initiator.SYSTEM)
                .transactionType(TransactionType.ROLLBACK)
                .method(Method.SYSTEM)
                .referenceCode(transactionId)
                .note(ROLLBACK_MESSAGE + transactionId)
                .description(ROLLBACK_MESSAGE + transactionId)
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        // Create transaction detail list
        rollback.setTransactionDetailList(List.of(
                // Master transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(masterAccount.getCustomerId())
                        .account(masterAccount.getAccountNumber())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(masterAccount.getBalance())
                        .currentBalance(masterAccount.getBalance().add(amount))
                        .availableBalance(masterAccount.getAvailableBalance().add(amount))
                        .direction(Direction.RECEIVE)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(masterAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build(),
                // Savings transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(savingsAccount.getCustomerId())
                        .account(savingsAccount.getAccountNumber())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(savingsBalance)
                        .currentBalance(savingsBalance.add(amount.negate()))
                        .availableBalance(savingsBalance.add(amount.negate()))
                        .direction(Direction.SEND)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(savingsAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build()));

        // Create transaction state list
        rollback.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        try {

            // Rollback core banking transaction (savings/master account)
            log.warn("Rollback core transaction (savings/master account) with code: {}",
                    transaction.get().getCoreRollbackCode());
            transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                    .senderAccountNumber(masterAccount.getAccountNumber())
                    .senderAmount(amount.negate())
                    .receiverAccountNumber(savingsAccount.getAccountNumber())
                    .receiverAmount(amount)
                    .masterAccountNumber(Constant.STRING)
                    .masterAmount(BigDecimal.ZERO)
                    .type(TransactionType.INTERNAL)
                    .referenceCode(transaction.get().getCoreRollbackCode())
                    .build());

            // Update savings account balance
            accountDubboService.updateBalance(savingsAccount.getAccountNumber(), amount.negate());

            // Save transaction
            transactionRepository.save(rollback);
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Rollback fail");
        }

        log.info("Rollback savings transaction successful");
        return true;
    }

    @Override
    public TransactionSavingsResultDTO createSavingsClosingTransaction(CreateSavingsPaymentTransactionDTO create)
            throws DubboException {

        // Check validation
        log.info("Entering createSavingsClosingTransaction with parameters: create = {}", create.toString());
        Set<ConstraintViolation<Object>> violations = validator.validate(create);
        if (!violations.isEmpty()) {

            String errors = ErrorMapGenerator.violationToErrorMap(violations);
            log.error(errors);
            throw new DubboException(errors);
        }

        // Check customer (Customer service)
        try {

            CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(create.getCifCode());

            if (customer == null || !customer.isActive())
                throw new Exception();

            log.info("Customer info: {}", customer);
        } catch (Exception e) {

            log.error("Invalid customer");
            throw new DubboException("Invalid customer");
        }

        // Check payment account validation (Account service)
        AccountInfoDTO paymentAccount;
        try {

            paymentAccount = accountDubboService.getAccountDetail(create.getPaymentAccount());

            if (paymentAccount == null || !paymentAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Payment account information with account number({}): {}", create.getPaymentAccount(), paymentAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid payment account");
            throw new DubboException("Invalid payment account");
        }

        // Check savings account validation (Account service)
        AccountInfoDTO savingsAccount;
        try {

            savingsAccount = accountDubboService.getAccountDetail(create.getSavingAccount());

            if (savingsAccount == null || !savingsAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Savings account information with account number({}): {}", create.getSavingAccount(), savingsAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid savings account");
            throw new DubboException("Invalid savings account");
        }

        log.info("Transaction amount");
        BigDecimal amount = savingsAccount.getCurrentAccountBalance();
        log.info("Amount: {}", amount);
        BigDecimal paymentBalance = paymentAccount.getCurrentAccountBalance();
        log.info("Payment balance: {}", paymentBalance);
        log.info("Current payment balance: {}", paymentBalance.add(amount));
        BigDecimal savingsBalance = savingsAccount.getCurrentAccountBalance();
        log.info("Savings balance: {}", savingsBalance);
        log.info("Current savings balance: {}", savingsBalance.add(amount.negate()));

        // Create transaction
        String id = new ULID().nextULID();
        InternalTransaction transaction = InternalTransaction.builder()
                .id(id)
                .type(Type.TRANSFER)
                .cifCode(create.getCifCode())
                // Payment account (sender) information
                .senderAccountId(savingsAccount.getAccountId())
                .senderAccount(savingsAccount.getAccountNumber())
                .senderAccountType(savingsAccount.getAccountType())
                // Savings account (receiver) information
                .receiverAccountId(paymentAccount.getAccountId())
                .receiverAccount(paymentAccount.getAccountNumber())
                .receiverAccountType(paymentAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(Initiator.CUSTOMER)
                .transactionType(TransactionType.INTERNAL)
                .method(Method.ONLINE_BANKING)
                .note(create.getNote())
                .description(create.getDescription())
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        // Create transaction detail list
        transaction.setTransactionDetailList(List.of(
                // Savings transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(savingsAccount.getCustomerId())
                        .account(create.getSavingAccount())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(savingsBalance)
                        .currentBalance(savingsBalance.add(amount).negate())
                        .availableBalance(savingsBalance.add(amount).negate())
                        .direction(Direction.SEND)
                        .description(create.getDescription())
                        .status(true)
                        .build(),
                // Payment transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(paymentAccount.getCustomerId())
                        .account(create.getPaymentAccount())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(paymentBalance)
                        .currentBalance(paymentBalance.add(amount))
                        .availableBalance(paymentBalance.add(amount))
                        .direction(Direction.RECEIVE)
                        .description(create.getDescription())
                        .status(true)
                        .build()));

        // Create transaction state list
        transaction.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        // Update account balance (Core banking service)
        try {

            // Update payment/savings account balance (Core banking)
            ResponseEntity<TransactionCoreNapasDTO> rsCoreTransaction = transactionCoreFeignClient.createInternal
                    (CreateInternalTransactionDTO.builder()
                            .senderAccountNumber(savingsAccount.getAccountNumber())
                            .senderAmount(amount.negate())
                            .receiverAccountNumber(paymentAccount.getAccountNumber())
                            .receiverAmount(amount)
                            .masterAccountNumber(Constant.STRING)
                            .fee(BigDecimal.ZERO)
                            .note(create.getNote())
                            .description(create.getDescription())
                            .build());
            if (rsCoreTransaction != null) {

                log.info("Core transaction information: {}", rsCoreTransaction);
                if (rsCoreTransaction.getBody() != null)
                    transaction.setCoreRollbackCode(rsCoreTransaction.getBody().getReferenceCode());
            }

        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Core banking service not available");
        }

        // Update account balance (Account service)
        int count = 0;
        try {

            // Update payment account balance
            AccountBalanceDTO payment = accountDubboService.updateBalance(create.getPaymentAccount(), amount);
            count++;

            // Update payment account balance
            AccountBalanceDTO savings = accountDubboService.updateBalance(create.getSavingAccount(), amount.negate());
            count++;

            // Save transaction
            transactionRepository.save(transaction);

            return TransactionSavingsResultDTO.builder()
                    .transactionId(id)
                    .balanceBankingAccount(payment.getBalance())
                    .balanceSavingsAccount(savings.getBalance())
                    .build();
        } catch (Exception e) {

            if (count > 0) {

                // Rollback core banking transaction (savings/payment account)
                log.warn("Rollback core transaction (savings/payment account) with code: {}", transaction.getCoreRollbackCode());
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .senderAccountNumber(savingsAccount.getAccountNumber())
                        .senderAmount(amount.negate())
                        .receiverAccountNumber(paymentAccount.getAccountNumber())
                        .receiverAmount(amount)
                        .masterAccountNumber(Constant.STRING)
                        .masterAmount(BigDecimal.ZERO)
                        .type(TransactionType.INTERNAL)
                        .referenceCode(transaction.getCoreRollbackCode())
                        .build());
            }

            throw new DubboException("Update account balance fail");
        }
    }

    @Override
    public boolean rollbackSavingsClosingTransaction(String transactionId) throws DubboException {

        // Check transaction validation
        log.info("Entering rollbackSavingsClosingTransaction with parameters: transactionId = {}", transactionId);
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isPresent()) {

            log.info("Transaction is exist with transaction id: {}", transactionId);
        } else {

            log.error("Invalid transaction id");
            throw new DubboException("Invalid transaction id");
        }

        // Check payment account validation (Account service)
        AccountInfoDTO paymentAccount;
        try {

            paymentAccount = accountDubboService.getAccountDetail(transaction.get().getReceiverAccount());

            if (paymentAccount == null || !paymentAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Payment account information with account number({}): {}",
                    transaction.get().getReceiverAccount(), paymentAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid payment account");
            throw new DubboException("Invalid payment account");
        }

        // Check savings account validation (Account service)
        AccountInfoDTO savingsAccount;
        try {

            savingsAccount = accountDubboService.getAccountDetail(transaction.get().getSenderAccount());

            if (savingsAccount == null || !savingsAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Savings account information with account number({}): {}",
                    transaction.get().getSenderAccount(), savingsAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid savings account");
            throw new DubboException("Invalid savings account");
        }

        log.info("Transaction amount");
        BigDecimal amount = transaction.get().getAmount();
        log.info("Amount: {}", amount);
        BigDecimal paymentBalance = paymentAccount.getCurrentAccountBalance();
        log.info("Payment balance: {}", paymentBalance);
        log.info("Current payment balance: {}", paymentBalance.add(amount.negate()));
        BigDecimal savingsBalance = savingsAccount.getCurrentAccountBalance();
        log.info("Savings balance: {}", savingsBalance);
        log.info("Current savings balance: {}", savingsBalance.add(amount));

        // Create transaction
        InternalTransaction rollback = InternalTransaction.builder()
                .id(new ULID().nextULID())
                .type(Type.TRANSFER)
                .cifCode(transaction.get().getCifCode())
                // Payment account (sender) information
                .senderAccountId(paymentAccount.getAccountId())
                .senderAccount(paymentAccount.getAccountNumber())
                .senderAccountType(paymentAccount.getAccountType())
                // Savings account (receiver) information
                .receiverAccountId(savingsAccount.getAccountId())
                .receiverAccount(savingsAccount.getAccountNumber())
                .receiverAccountType(savingsAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(Initiator.CUSTOMER)
                .transactionType(TransactionType.ROLLBACK)
                .method(Method.ONLINE_BANKING)
                .referenceCode(transactionId)
                .note(ROLLBACK_MESSAGE + transactionId)
                .description(ROLLBACK_MESSAGE + transactionId)
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        // Create transaction detail list
        rollback.setTransactionDetailList(List.of(
                // Savings transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(savingsAccount.getCustomerId())
                        .account(savingsAccount.getAccountNumber())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(savingsBalance)
                        .currentBalance(savingsBalance.add(amount))
                        .availableBalance(savingsBalance.add(amount))
                        .direction(Direction.RECEIVE)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(savingsAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build(),
                // Payment transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(paymentAccount.getCustomerId())
                        .account(paymentAccount.getAccountNumber())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(paymentBalance)
                        .currentBalance(paymentBalance.add(amount.negate()))
                        .availableBalance(paymentBalance.add(amount.negate()))
                        .direction(Direction.SEND)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(paymentAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build()));

        // Create transaction state list
        rollback.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        try {

            // Rollback core banking transaction (savings/payment account)
            log.warn("Rollback core transaction (savings/payment account) with code: {}",
                    transaction.get().getCoreRollbackCode());
            transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                    .senderAccountNumber(savingsAccount.getAccountNumber())
                    .senderAmount(amount.negate())
                    .receiverAccountNumber(paymentAccount.getAccountNumber())
                    .receiverAmount(amount)
                    .masterAccountNumber(Constant.STRING)
                    .masterAmount(BigDecimal.ZERO)
                    .type(TransactionType.INTERNAL)
                    .referenceCode(transaction.get().getCoreRollbackCode())
                    .build());

            // Update payment account balance
            accountDubboService.updateBalance(paymentAccount.getAccountNumber(), amount.negate());

            // Update savings account balance
            accountDubboService.updateBalance(savingsAccount.getAccountNumber(), amount);

            // Save transaction
            transactionRepository.save(rollback);
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Rollback fail");
        }

        log.info("Rollback savings closing transaction successful");
        return true;
    }

    @Override
    public TransactionCreditResultDTO createCreditAccountDisbursement(CreateCreditDisbursementTransactionDTO create)
            throws DubboException {
//
//        // Check validation
//        log.info("Entering createCreditAccountDisbursement with parameters: create = {}", create.toString());
//        Set<ConstraintViolation<Object>> violations = validator.validate(create);
//        if (!violations.isEmpty()) {
//
//            String errors = ErrorMapGenerator.violationToErrorMap(violations);
//            log.error(errors);
//            throw new DubboException(errors);
//        }
//
//        // Check customer (Customer service)
//        try {
//
//            CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(create.getCifCode());
//
//            if (customer == null || !customer.isActive())
//                throw new Exception();
//
//            log.info("Customer info: {}", customer);
//        } catch (Exception e) {
//
//            log.error("Invalid customer");
//            throw new DubboException("Invalid customer");
//        }
//
//        // Check master account validation (Core banking)
//        AccountExtraCoreDTO masterAccount;
//        try {
//
//            ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
//                    (vaultConfig.getAccountNumber());
//            masterAccount = rsMasterAccount.getBody();
//
//            if (masterAccount == null || !masterAccount.getIsActive())
//                throw new Exception();
//
//            log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);
//        } catch (Exception e) {
//
//            log.error("Invalid master account");
//            throw new DubboException("Invalid master account");
//        }
//
//        // Check credit account validation (Account service)
//        AccountInfoDTO creditAccount;
//        try {
//
//            creditAccount = accountDubboService.getAccountDetail(create.getCreditAccount());
//
//            if (creditAccount == null || !creditAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
//                throw new Exception();
//
//            log.info("Credit account information with account number({}): {}", create.getCreditAccount(), creditAccount);
//        } catch (Exception e) {
//
//            if (e instanceof DubboException) {
//
//                log.error(e.getMessage());
//            }
//
//            log.error("Invalid credit account");
//            throw new DubboException("Invalid credit account");
//        }
//
//        log.info("Transaction amount");
//        BigDecimal amount = create.getAmount();
//        log.info("Amount: {}", amount);
//        BigDecimal creditBalance = creditAccount.getCurrentAccountBalance();
//        log.info("Credit balance: {}", creditBalance);
//        log.info("Current credit balance: {}", creditBalance.add(amount));
//
//        // Create transaction
//        String id = new ULID().nextULID();
//        InternalTransaction transaction = InternalTransaction.builder()
//                .id(id)
//                .type(Type.TRANSFER)
//                .cifCode(create.getCifCode())
//                // Master account (sender) information
//                .senderAccountId(masterAccount.getAccountId())
//                .senderAccount(masterAccount.getAccountNumber())
//                .senderAccountType(masterAccount.getAccountType())
//                // Credit account (receiver) information
//                .receiverAccountId(creditAccount.getAccountId())
//                .receiverAccount(creditAccount.getAccountNumber())
//                .receiverAccountType(creditAccount.getAccountType())
//                // Transaction information
//                .amount(amount)
//                .initiator(Initiator.CUSTOMER)
//                .transactionType(TransactionType.INTERNAL)
//                .method(Method.ONLINE_BANKING)
//                .note(create.getNote())
//                .description(create.getDescription())
//                .feePayer(FeePayer.SENDER)
//                .fee(BigDecimal.ZERO)
//                .status(true)
//                .build();
//
//        // Create transaction detail list
//        transaction.setTransactionDetailList(List.of(
//                // Credit transaction
//                TransactionDetail.builder()
//                        .id(new ULID().nextULID())
//                        .transaction(transaction)
//                        .customerId(creditAccount.getCustomerId())
//                        .account(create.getCreditAccount())
//                        .amount(amount)
//                        .fee(BigDecimal.ZERO)
//                        .netAmount(amount)
//                        .previousBalance(creditBalance)
//                        .currentBalance(creditBalance.add(amount))
//                        .availableBalance(creditBalance.add(amount))
//                        .direction(Direction.RECEIVE)
//                        .description(create.getDescription())
//                        .status(true)
//                        .build(),
//                // Payment transaction
//                TransactionDetail.builder()
//                        .id(new ULID().nextULID())
//                        .transaction(transaction)
//                        .customerId(masterAccount.getCustomerId())
//                        .account(masterAccount.getAccountNumber())
//                        .amount(amount.negate())
//                        .fee(BigDecimal.ZERO)
//                        .netAmount(amount.negate())
//                        .previousBalance(masterAccount.getBalance())
//                        .currentBalance(masterAccount.getBalance().add(amount.negate()))
//                        .availableBalance(masterAccount.getBalance().add(amount.negate()))
//                        .direction(Direction.SEND)
//                        .description(create.getDescription())
//                        .status(true)
//                        .build()));
//
//        // Create transaction state list
//        transaction.setTransactionStateList(List.of(
//                TransactionState.builder()
//                        .id(new ULID().nextULID())
//                        .transaction(transaction)
//                        .state(State.COMPLETED)
//                        .description(State.COMPLETED.getDescription())
//                        .status(true)
//                        .build()));
//
//        // Update account balance (Core banking service)
//        try {
//
//            // Update payment/savings account balance (Core banking)
//            ResponseEntity<TransactionCoreNapasDTO> rsCoreTransaction = transactionCoreFeignClient.createInternal
//                    (CreateInternalTransactionDTO.builder()
//                            .senderAccountNumber(paymentAccount.getAccountNumber())
//                            .senderAmount(amount.negate())
//                            .receiverAccountNumber(savingsAccount.getAccountNumber())
//                            .receiverAmount(amount)
//                            .masterAccountNumber(Constant.STRING)
//                            .fee(BigDecimal.ZERO)
//                            .note(create.getNote())
//                            .description(create.getDescription())
//                            .build());
//            if (rsCoreTransaction != null) {
//
//                log.info("Core transaction information: {}", rsCoreTransaction);
//                if (rsCoreTransaction.getBody() != null)
//                    transaction.setCoreRollbackCode(rsCoreTransaction.getBody().getReferenceCode());
//            }
//
//        } catch (Exception e) {
//
//            log.error(e.getMessage());
//            throw new DubboException("Core banking service not available");
//        }

        return null;
    }

    @Override
    public boolean rollbackCreditAccountDisbursement(String transactionId) throws DubboException {
        return false;
    }

    @Override
    public TransactionCreditResultDTO createCreditTransaction(CreateCreditTransactionDTO create)
            throws DubboException {

        // Check validation
        log.info("Entering createCreditTransaction with parameters: create = {}", create.toString());
        Set<ConstraintViolation<Object>> violations = validator.validate(create);
        if (!violations.isEmpty()) {

            String errors = ErrorMapGenerator.violationToErrorMap(violations);
            log.error(errors);
            throw new DubboException(errors);
        }

        // Check customer (Customer service)
        try {

            CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(create.getCifCode());

            if (customer == null || !customer.isActive())
                throw new Exception();

            log.info("Customer info: {}", customer);
        } catch (Exception e) {

            log.error("Invalid customer");
            throw new DubboException("Invalid customer");
        }

        // Check credit account validation (Account service)
        AccountInfoDTO creditAccount;
        try {

            creditAccount = accountDubboService.getAccountDetail(create.getCreditAccount());

            if (creditAccount == null || !creditAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Credit account information with account number({}): {}", create.getCreditAccount(), creditAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid credit account");
            throw new DubboException("Invalid credit account");
        }

        log.info("Transaction amount");
        BigDecimal amount = create.getAmount();
        log.info("Amount: {}", amount);
        BigDecimal creditBalance = creditAccount.getDebtBalance();
        log.info("Credit balance: {}", creditBalance);
        log.info("Current credit balance: {}", creditBalance.add(amount));

        // Create transaction
        String id = new ULID().nextULID();
        InternalTransaction transaction = InternalTransaction.builder()
                .id(id)
                .type(Type.TRANSFER)
                .cifCode(create.getCifCode())
                // Credit account (sender) information
                .senderAccountId(creditAccount.getAccountId())
                .senderAccount(creditAccount.getAccountNumber())
                .senderAccountType(creditAccount.getAccountType())
                // Credit account (receiver) information
                .receiverAccountId(creditAccount.getAccountId())
                .receiverAccount(creditAccount.getAccountNumber())
                .receiverAccountType(creditAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(Initiator.SYSTEM)
                .transactionType(TransactionType.SYSTEM)
                .method(Method.SYSTEM)
                .note(create.getNote())
                .description(create.getDescription())
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        // Create transaction detail list
        transaction.setTransactionDetailList(List.of(
                // Loan transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(creditAccount.getCustomerId())
                        .account(create.getCreditAccount())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(creditBalance)
                        .currentBalance(creditBalance.add(amount))
                        .availableBalance(creditBalance.add(amount))
                        .direction(Direction.RECEIVE)
                        .description(create.getDescription())
                        .status(true)
                        .build()));

        // Create transaction state list
        transaction.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        // Update account balance (Core banking service)
        StringBuilder sb = new StringBuilder();
        try {

            // Update credit account balance (Core banking)
            ResponseEntity<TransactionCoreNapasDTO> rsLoanCoreTransaction = transactionCoreFeignClient.createExternal
                    (CreateExternalTransactionDTO.builder()
                            .accountNumber(creditAccount.getAccountNumber())
                            .amount(amount)
                            .masterAccountNumber(Constant.STRING)
                            .fee(BigDecimal.ZERO)
                            .note(create.getNote())
                            .description(create.getDescription())
                            .build());
            if (rsLoanCoreTransaction != null) {

                log.info("Credit core transaction information: {}", rsLoanCoreTransaction);
                if (rsLoanCoreTransaction.getBody() != null)
                    sb.append(rsLoanCoreTransaction.getBody().getReferenceCode());
            }

            transaction.setCoreRollbackCode(sb.toString());
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Core banking service not available");
        }

        int count = 0;
        try {

            AccountBalanceDTO credit = accountDubboService.updateBalance(create.getCreditAccount(), amount);
            count++;

            // Save transaction
            transactionRepository.save(transaction);

            return TransactionCreditResultDTO.builder()
                    .transactionId(id)
                    .balanceCreditAccount(credit.getBalance())
                    .build();
        } catch (Exception e) {

            if (count > 0 || !Strings.isNullOrEmpty(transaction.getCoreRollbackCode())) {

                // Rollback core banking transaction (credit account)
                log.warn("Rollback core transaction (credit account) with code: {}", transaction.getCoreRollbackCode());
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .customerAccountNumber(creditAccount.getAccountNumber())
                        .customerAmount(amount)
                        .masterAccountNumber(Constant.STRING)
                        .masterAmount(BigDecimal.ZERO)
                        .type(TransactionType.EXTERNAL)
                        .referenceCode(transaction.getCoreRollbackCode())
                        .build());
            }

            throw new DubboException("Update account balance fail");
        }
    }

    @Override
    public boolean rollbackCreditTransaction(String transactionId) throws DubboException {

        // Check transaction validation
        log.info("Entering rollbackCreditTransaction with parameters: transactionId = {}", transactionId);
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isPresent()) {

            log.info("Transaction is exist with transaction id: {}", transactionId);
        } else {

            log.error("Invalid transaction id");
            throw new DubboException("Invalid transaction id");
        }

        // Check credit account validation (Account service)
        AccountInfoDTO creditAccount;
        try {

            creditAccount = accountDubboService.getAccountDetail(transaction.get().getSenderAccount());

            if (creditAccount == null || !creditAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Credit account information with account number({}): {}", transaction.get().getSenderAccount(), creditAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid credit account");
            throw new DubboException("Invalid credit account");
        }

        log.info("Transaction amount");
        BigDecimal amount = transaction.get().getAmount();
        log.info("Amount: {}", amount);
        BigDecimal creditBalance = creditAccount.getDebtBalance();
        log.info("Credit balance: {}", creditBalance);
        log.info("Current credit balance: {}", creditBalance.add(amount.negate()));

        // Create transaction
        InternalTransaction rollback = InternalTransaction.builder()
                .id(new ULID().nextULID())
                .type(Type.TRANSFER)
                .cifCode(transaction.get().getCifCode())
                // Credit account (sender) information
                .senderAccountId(creditAccount.getAccountId())
                .senderAccount(creditAccount.getAccountNumber())
                .senderAccountType(creditAccount.getAccountType())
                // Credit account (receiver) information
                .receiverAccountId(creditAccount.getAccountId())
                .receiverAccount(creditAccount.getAccountNumber())
                .receiverAccountType(creditAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(Initiator.SYSTEM)
                .transactionType(TransactionType.ROLLBACK)
                .method(Method.SYSTEM)
                .referenceCode(transactionId)
                .note(ROLLBACK_MESSAGE + transactionId)
                .description(ROLLBACK_MESSAGE + transactionId)
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        // Create transaction detail list
        rollback.setTransactionDetailList(List.of(
                // Credit transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(creditAccount.getCustomerId())
                        .account(creditAccount.getAccountNumber())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(creditBalance)
                        .currentBalance(creditBalance.add(amount.negate()))
                        .availableBalance(creditBalance.add(amount.negate()))
                        .direction(Direction.SEND)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(creditAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build()));

        // Create transaction state list
        rollback.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        try {

            // Rollback core banking transaction (credit account)
            log.warn("Rollback core transaction (credit account) with code: {}", transaction.get().getCoreRollbackCode());
            transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                    .customerAccountNumber(creditAccount.getAccountNumber())
                    .customerAmount(amount)
                    .masterAccountNumber(Constant.STRING)
                    .masterAmount(BigDecimal.ZERO)
                    .type(TransactionType.EXTERNAL)
                    .referenceCode(transaction.get().getCoreRollbackCode())
                    .build());

            // Update credit account balance
            accountDubboService.updateBalance(creditAccount.getAccountNumber(), amount.negate());

            // Save transaction
            transactionRepository.save(rollback);
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Rollback fail");
        }

        log.info("Rollback credit transaction successful");
        return true;
    }

    @Override
    public TransactionCreditResultDTO createCreditPaymentTransaction(CreateCreditPaymentTransactionDTO create)
            throws DubboException {

        // Check validation
        log.info("Entering createCreditPaymentTransaction with parameters: create = {}", create.toString());
        Set<ConstraintViolation<Object>> violations = validator.validate(create);
        if (!violations.isEmpty()) {

            String errors = ErrorMapGenerator.violationToErrorMap(violations);
            log.error(errors);
            throw new DubboException(errors);
        }

        // Check customer (Customer service)
        try {

            CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(create.getCifCode());

            if (customer == null || !customer.isActive())
                throw new Exception();

            log.info("Customer info: {}", customer);
        } catch (Exception e) {

            log.error("Invalid customer");
            throw new DubboException("Invalid customer");
        }

        // Check master account validation (Core banking)
        AccountExtraCoreDTO masterAccount;
        try {

            ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
                    (vaultConfig.getAccountNumber());
            masterAccount = rsMasterAccount.getBody();

            if (masterAccount == null || !masterAccount.getIsActive())
                throw new Exception();

            log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);
        } catch (Exception e) {

            log.error("Invalid master account");
            throw new DubboException("Invalid master account");
        }

        // Check payment account validation (Account service)
        AccountInfoDTO paymentAccount;
        try {

            paymentAccount = accountDubboService.getAccountDetail(create.getPaymentAccount());

            if (paymentAccount == null || !paymentAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Payment account information with account number({}): {}", create.getPaymentAccount(), paymentAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid payment account");
            throw new DubboException("Invalid payment account");
        }

        // Check credit account validation (Account service)
        AccountInfoDTO creditAccount;
        try {

            creditAccount = accountDubboService.getAccountDetail(create.getCreditAccount());

            if (creditAccount == null || !creditAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Credit account information with account number({}): {}", create.getCreditAccount(), creditAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid credit account");
            throw new DubboException("Invalid credit account");
        }

        if (create.getAmount().compareTo(paymentAccount.getCurrentAccountBalance()) > 0) {

            log.info("Payment amount exceeds payment balance");
            throw new DubboException("Payment amount exceeds payment balance");
        }

        if (create.getAmount().compareTo(creditAccount.getDebtBalance().abs()) > 0) {

            log.info("Payment amount exceeds credit balance");
            throw new DubboException("Payment amount exceeds credit balance");
        }

        log.info("Transaction amount");
        BigDecimal amount = create.getAmount();
        log.info("Amount: {}", amount);
        BigDecimal creditBalance = creditAccount.getDebtBalance();
        log.info("Credit balance: {}", creditBalance);
        log.info("Current credit balance: {}", creditBalance.add(amount.negate()));
        BigDecimal paymentBalance = paymentAccount.getCurrentAccountBalance();
        log.info("Payment balance: {}", paymentBalance);
        log.info("Current payment balance: {}", paymentBalance.add(amount.negate()));

        // Create transaction
        String id = new ULID().nextULID();
        InternalTransaction transaction = InternalTransaction.builder()
                .id(id)
                .type(Type.TRANSFER)
                .cifCode(create.getCifCode())
                // Credit account (sender) information
                .senderAccountId(creditAccount.getAccountId())
                .senderAccount(creditAccount.getAccountNumber())
                .senderAccountType(creditAccount.getAccountType())
                // Master account (receiver) information
                .receiverAccountId(masterAccount.getAccountId())
                .receiverAccount(masterAccount.getAccountNumber())
                .receiverAccountType(masterAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(Initiator.CUSTOMER)
                .transactionType(TransactionType.INTERNAL)
                .method(Method.ONLINE_BANKING)
                .note(create.getNote())
                .description(create.getDescription())
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        transaction.setTransactionDetailList(List.of(
                // Credit transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(creditAccount.getCustomerId())
                        .account(create.getCreditAccount())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(creditBalance)
                        .currentBalance(creditBalance.add(amount.negate()))
                        .availableBalance(creditBalance.add(amount.negate()))
                        .direction(Direction.SEND)
                        .description(create.getDescription())
                        .status(true)
                        .build(),
                // Master transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(masterAccount.getCustomerId())
                        .account(masterAccount.getAccountNumber())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(masterAccount.getBalance())
                        .currentBalance(masterAccount.getBalance().add(amount))
                        .availableBalance(masterAccount.getAvailableBalance().add(amount))
                        .direction(Direction.RECEIVE)
                        .description(create.getDescription())
                        .status(true)
                        .build(),
                // Payment transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(paymentAccount.getCustomerId())
                        .account(create.getPaymentAccount())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(paymentBalance)
                        .currentBalance(paymentBalance.add(amount.negate()))
                        .availableBalance(paymentBalance.add(amount.negate()))
                        .direction(Direction.SEND)
                        .description(create.getDescription())
                        .status(true)
                        .build()
        ));

        // Create transaction state list
        transaction.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        // Update account balance (Core banking service)
        StringBuilder sb = new StringBuilder();
        try {

            // Update payment/master account balance (Core banking)
            ResponseEntity<TransactionCoreNapasDTO> rsCoreTransaction = transactionCoreFeignClient.createInternal
                    (CreateInternalTransactionDTO.builder()
                            .senderAccountNumber(paymentAccount.getAccountNumber())
                            .senderAmount(amount.negate())
                            .receiverAccountNumber(masterAccount.getAccountNumber())
                            .receiverAmount(amount)
                            .masterAccountNumber(Constant.STRING)
                            .fee(BigDecimal.ZERO)
                            .note(create.getNote())
                            .description(create.getDescription())
                            .build());
            if (rsCoreTransaction != null) {

                log.info("Core transaction information: {}", rsCoreTransaction);
                if (rsCoreTransaction.getBody() != null)
                    sb.append(rsCoreTransaction.getBody().getReferenceCode());
            }

            // Update credit account balance (Core banking)
            ResponseEntity<TransactionCoreNapasDTO> rsLoanCoreTransaction = transactionCoreFeignClient.createExternal
                    (CreateExternalTransactionDTO.builder()
                            .accountNumber(creditAccount.getAccountNumber())
                            .amount(amount.negate())
                            .masterAccountNumber(Constant.STRING)
                            .fee(BigDecimal.ZERO)
                            .note(create.getNote())
                            .description(create.getDescription())
                            .build());
            if (rsLoanCoreTransaction != null) {

                sb.append("|");
                log.info("Credit core transaction information: {}", rsLoanCoreTransaction);
                if (rsLoanCoreTransaction.getBody() != null)
                    sb.append(rsLoanCoreTransaction.getBody().getReferenceCode());
            }

            transaction.setCoreRollbackCode(sb.toString());
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Core banking service not available");
        }

        // Get list reference code
        int count = 0;
        String[] refs = sb.toString().split("\\|");
        log.info("List of reference code: {}", Arrays.stream(refs).toList());

        try {

            if (refs.length != 2)
                throw new Exception();

            // Update payment account balance
            AccountBalanceDTO payment = accountDubboService.updateBalance(create.getPaymentAccount(), amount.negate());
            count++;

            // Update credit account balance
            AccountBalanceDTO credit = accountDubboService.updateBalance(create.getCreditAccount(), amount.negate());
            count++;

            // Save transaction
            transactionRepository.save(transaction);

            return TransactionCreditResultDTO.builder()
                    .transactionId(id)
                    .balanceBankingAccount(payment.getBalance())
                    .balanceCreditAccount(credit.getBalance())
                    .build();
        } catch (Exception e) {

            if (count > 0 || refs.length > 0) {

                // Rollback core banking transaction (master/payment account)
                log.warn("Rollback core transaction (master/payment account) with code: {}", refs[0]);
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .senderAccountNumber(paymentAccount.getAccountNumber())
                        .senderAmount(amount.negate())
                        .receiverAccountNumber(masterAccount.getAccountNumber())
                        .receiverAmount(amount)
                        .masterAccountNumber(Constant.STRING)
                        .masterAmount(BigDecimal.ZERO)
                        .type(TransactionType.INTERNAL)
                        .referenceCode(refs[0])
                        .build());
            }

            if (count > 1 || refs.length > 1) {

                // Rollback core banking transaction (credit account)
                log.warn("Rollback core transaction (credit account) with code: {}", refs[1]);
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .customerAccountNumber(creditAccount.getAccountNumber())
                        .customerAmount(amount.negate())
                        .masterAccountNumber(Constant.STRING)
                        .masterAmount(BigDecimal.ZERO)
                        .type(TransactionType.EXTERNAL)
                        .referenceCode(refs[1])
                        .build());
            }

            throw new DubboException("Update account balance fail");
        }
    }

    @Override
    public boolean rollbackCreditPaymentTransaction(String transactionId) throws DubboException {

        // Check transaction validation
        log.info("Entering rollbackCreditPaymentTransaction with parameters: transactionId = {}", transactionId);
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isPresent()) {

            log.info("Transaction is exist with transaction id: {}", transactionId);
        } else {

            log.error("Invalid transaction id");
            throw new DubboException("Invalid transaction id");
        }

        // Check master account (Core banking)
        ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
                (vaultConfig.getAccountNumber());
        AccountExtraCoreDTO masterAccount = rsMasterAccount.getBody();
        if (masterAccount == null || !masterAccount.getIsActive()) {

            log.error("Invalid master account");
            throw new DubboException("Invalid master account");
        }
        log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);

        // Check payment account validation (Account service)
        AccountInfoDTO paymentAccount;
        try {

            String paymentAccountNum = transaction.get().getTransactionDetailList()
                    .stream().filter(t -> t.getDirection().equals(Direction.SEND)
                            && !t.getAccount().equals(transaction.get().getSenderAccount())).findFirst()
                    .orElseThrow().getAccount();
            paymentAccount = accountDubboService.getAccountDetail(paymentAccountNum);

            if (paymentAccount == null || !paymentAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Payment account information with account number({}): {}", paymentAccountNum, paymentAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid payment account");
            throw new DubboException("Invalid payment account");
        }

        // Check credit account validation (Account service)
        AccountInfoDTO creditAccount;
        try {

            creditAccount = accountDubboService.getAccountDetail(transaction.get().getSenderAccount());

            if (creditAccount == null || !creditAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Credit account information with account number({}): {}",
                    transaction.get().getSenderAccount(), creditAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid credit account");
            throw new DubboException("Invalid credit account");
        }

        log.info("Transaction amount");
        BigDecimal amount = transaction.get().getAmount();
        log.info("Amount: {}", amount);
        BigDecimal creditBalance = creditAccount.getDebtBalance();
        log.info("Credit balance: {}", creditBalance);
        log.info("Current credit balance: {}", creditBalance.add(amount));
        BigDecimal paymentBalance = paymentAccount.getCurrentAccountBalance();
        log.info("Payment balance: {}", paymentBalance);
        log.info("Current payment balance: {}", paymentBalance.add(amount));

        // Create transaction
        InternalTransaction rollback = InternalTransaction.builder()
                .id(new ULID().nextULID())
                .type(Type.TRANSFER)
                .cifCode(transaction.get().getCifCode())
                // Master account (sender) information
                .senderAccountId(masterAccount.getAccountId())
                .senderAccount(masterAccount.getAccountNumber())
                .senderAccountType(masterAccount.getAccountType())
                // Credit account (receiver) information
                .receiverAccountId(creditAccount.getAccountId())
                .receiverAccount(creditAccount.getAccountNumber())
                .receiverAccountType(creditAccount.getAccountType())
                // Transaction information
                .amount(amount)
                .initiator(Initiator.CUSTOMER)
                .transactionType(TransactionType.ROLLBACK)
                .method(Method.ONLINE_BANKING)
                .referenceCode(transactionId)
                .note(ROLLBACK_MESSAGE + transactionId)
                .description(ROLLBACK_MESSAGE + transactionId)
                .feePayer(FeePayer.SENDER)
                .fee(BigDecimal.ZERO)
                .status(true)
                .build();

        rollback.setTransactionDetailList(List.of(
                // Credit transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(creditAccount.getCustomerId())
                        .account(creditAccount.getAccountNumber())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(creditBalance)
                        .currentBalance(creditBalance.add(amount))
                        .availableBalance(creditBalance.add(amount))
                        .direction(Direction.SEND)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(creditAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build(),
                // Master transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(masterAccount.getCustomerId())
                        .account(masterAccount.getAccountNumber())
                        .amount(amount.negate())
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount.negate())
                        .previousBalance(masterAccount.getBalance())
                        .currentBalance(masterAccount.getBalance().add(amount.negate()))
                        .availableBalance(masterAccount.getAvailableBalance().add(amount.negate()))
                        .direction(Direction.RECEIVE)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(masterAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build(),
                // Payment transaction
                TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(paymentAccount.getCustomerId())
                        .account(paymentAccount.getAccountNumber())
                        .amount(amount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(amount)
                        .previousBalance(paymentBalance)
                        .currentBalance(paymentBalance.add(amount))
                        .availableBalance(paymentBalance.add(amount))
                        .direction(Direction.SEND)
                        .description(ROLLBACK_MESSAGE + transaction.get()
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(paymentAccount.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build()
        ));

        // Create transaction state list
        rollback.setTransactionStateList(List.of(
                TransactionState.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .state(State.COMPLETED)
                        .description(State.COMPLETED.getDescription())
                        .status(true)
                        .build()));

        try {

            // Get list reference code
            String[] refs = transaction.get().getCoreRollbackCode().split("\\|");
            log.info("List of reference code: {}", Arrays.stream(refs).toList());

            // Rollback core banking transaction (master/payment account)
            log.warn("Rollback core transaction (master/payment account) with code: {}", refs[0]);
            transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                    .senderAccountNumber(paymentAccount.getAccountNumber())
                    .senderAmount(amount.negate())
                    .receiverAccountNumber(masterAccount.getAccountNumber())
                    .receiverAmount(amount)
                    .masterAccountNumber(Constant.STRING)
                    .masterAmount(BigDecimal.ZERO)
                    .type(TransactionType.INTERNAL)
                    .referenceCode(refs[0])
                    .build());

            // Rollback core banking transaction (credit account)
            log.warn("Rollback core transaction (credit account) with code: {}", refs[1]);
            transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                    .customerAccountNumber(creditAccount.getAccountNumber())
                    .customerAmount(amount.negate())
                    .masterAccountNumber(Constant.STRING)
                    .masterAmount(BigDecimal.ZERO)
                    .type(TransactionType.EXTERNAL)
                    .referenceCode(refs[1])
                    .build());

            // Update payment account balance
            accountDubboService.updateBalance(paymentAccount.getAccountNumber(), amount);

            // Update credit account balance
            accountDubboService.updateBalance(creditAccount.getAccountNumber(), amount);

            // Save transaction
            transactionRepository.save(rollback);
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new DubboException("Rollback fail");
        }

        log.info("Rollback credit payment transaction successful");
        return true;
    }
}
