package com.system.core_banking_service.service;

import com.system.common_library.dto.request.transaction.CoreTransactionRollbackDTO;
import com.system.common_library.dto.request.transaction.CreateExternalTransactionDTO;
import com.system.common_library.dto.request.transaction.CreateInternalTransactionDTO;
import com.system.common_library.dto.request.transaction.CreateSystemTransactionDTO;
import com.system.common_library.dto.response.transaction.TransactionCoreNapasDTO;
import com.system.common_library.enums.Direction;
import com.system.common_library.enums.TransactionType;
import com.system.common_library.exception.GroupValidationException;
import com.system.core_banking_service.entity.Account;
import com.system.core_banking_service.entity.Transaction;
import com.system.core_banking_service.mapper.TransactionMapper;
import com.system.core_banking_service.repository.AccountRepository;
import com.system.core_banking_service.repository.TransactionRepository;
import com.system.core_banking_service.service.interfaces.TransactionService;
import com.system.core_banking_service.util.Constant;
import de.huxhorn.sulky.ulid.ULID;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final Validator validator;

    private final MessageSource messageSource;

    private final TransactionMapper transactionMapper;

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    private static final String ROLLBACK_MESSAGE = "Rollback transaction id : ";

    @Override
    public TransactionCoreNapasDTO createExternal(CreateExternalTransactionDTO create) {

        try {

            // Check account number
            log.info("Entering createExternal with parameters: create = {}", create.toString());
            if (new HashSet<>(List.of(create.getAccountNumber(), create.getMasterAccountNumber())).size() != 2) {

                log.error("Account number is duplicate in list: {}",
                        List.of(create.getAccountNumber(), create.getMasterAccountNumber()));
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.DUPLICATE_ACCOUNT_NUMBER,
                                null, LocaleContextHolder.getLocale()));
            }

            // Check amount
            log.info("Transaction amount: {}", create.getAmount());
            if (create.getAmount().compareTo(BigDecimal.ZERO) == 0) {

                log.error("Amount is 0");
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.INVALID_AMOUNT, null, LocaleContextHolder.getLocale()));
            }

            // Generate reference code
            String referenceCode = new ULID().nextULID();
            log.info("Reference code: {}", referenceCode);

            // Get account
            Optional<Account> account = accountRepository.findByAccountNumberAndStatus
                    (create.getAccountNumber(), true);

            // Check account
            if (account.isPresent() && account.get().getIsActive()) {

                log.info("Account is exist with account number: {}", create.getAccountNumber());
                if (create.getFee().compareTo(BigDecimal.ZERO) > 0) {

                    // Get master account
                    Optional<Account> master = accountRepository.findByAccountNumberAndStatus
                            (create.getMasterAccountNumber(), true);

                    // Check master account
                    if (master.isPresent() && master.get().getIsActive()) {

                        log.info("Account is exist with account number: {}", create.getMasterAccountNumber());
                        BigDecimal fee = create.getFee();

                        // Create master transaction
                        Transaction masterTransaction = Transaction.builder()
                                .id(new ULID().nextULID())
                                .account(master.get())
                                .amount(fee)
                                .previousBalance(master.get().getBalance())
                                .currentBalance(master.get().getBalance().add(fee))
                                .availableBalance(master.get().getAvailableBalance().add(fee))
                                .referenceCode(referenceCode)
                                .type(TransactionType.EXTERNAL)
                                .direction(Direction.RECEIVE)
                                .note(create.getNote())
                                .description(create.getDescription())
                                .state(true)
                                .status(true)
                                .build();

                        log.info("Before (Account number: {})", create.getMasterAccountNumber());
                        log.info("Balance: {}", master.get().getBalance());
                        log.info("Available balance: {}", master.get().getAvailableBalance());

                        // Update master account balance
                        master.get().setLastTransactionDate(LocalDateTime.now());
                        master.get().setBalance(master.get().getBalance().add(fee));
                        master.get().setAvailableBalance(master.get().getAvailableBalance().add(fee));
                        master.get().setTotalIncome(master.get().getTotalIncome().add(fee));

                        log.info("After (Account number: {})", create.getMasterAccountNumber());
                        log.info("Balance: {}", master.get().getBalance());
                        log.info("Available balance: {}", master.get().getAvailableBalance());

                        transactionRepository.save(masterTransaction);
                    } else {

                        log.error("Invalid master account");
                        throw new InvalidParameterException(
                                messageSource.getMessage(
                                        Constant.INVALID_MASTER_ACCOUNT, null, LocaleContextHolder.getLocale()));
                    }
                }

                BigDecimal amount = create.getAmount();
                boolean isSender = amount.compareTo(BigDecimal.ZERO) < 0;
                BigDecimal balance = account.get().getAvailableBalance();

                // Check balance
                if (isSender && balance.compareTo(amount.negate()) < 0) {

                    log.error("Insufficient account balance to complete transaction");
                    throw new InvalidParameterException(
                            messageSource.getMessage(
                                    Constant.INSUFFICIENT_BALANCE, null, LocaleContextHolder.getLocale()));
                }

                // Create transaction
                Transaction transaction = Transaction.builder()
                        .id(new ULID().nextULID())
                        .account(account.get())
                        .amount(amount)
                        .previousBalance(account.get().getBalance())
                        .currentBalance(account.get().getBalance().add(amount))
                        .availableBalance(balance.add(amount))
                        .referenceCode(referenceCode)
                        .type(TransactionType.EXTERNAL)
                        .direction(isSender ? Direction.SEND : Direction.RECEIVE)
                        .note(create.getNote())
                        .description(create.getDescription())
                        .state(true)
                        .status(true)
                        .build();

                log.info("Before (Account number: {})", create.getAccountNumber());
                log.info("Balance: {}", account.get().getBalance());
                log.info("Available balance: {}", account.get().getAvailableBalance());

                // Update account balance
                account.get().setLastTransactionDate(LocalDateTime.now());
                account.get().setBalance(account.get().getBalance().add(amount));
                account.get().setAvailableBalance(balance.add(amount));

                log.info("After (Account number: {})", create.getAccountNumber());
                log.info("Balance: {}", account.get().getBalance());
                log.info("Available balance: {}", account.get().getAvailableBalance());

                if (isSender) {

                    account.get().setTotalExpenditure(account.get().getTotalExpenditure().add(amount));
                } else {

                    account.get().setTotalIncome(account.get().getTotalIncome().add(amount));
                }

                return transactionMapper.entityToDTO(transactionRepository.save(transaction));
            } else {

                log.error("Invalid account");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_ACCOUNT, null, LocaleContextHolder.getLocale()));
            }
        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.CREATE_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public TransactionCoreNapasDTO createInternal(CreateInternalTransactionDTO create) {

        try {

            // Check account number
            log.info("Entering createInternal with parameters: create = {}", create.toString());
            if (new HashSet<>(List.of(create.getSenderAccountNumber(), create.getReceiverAccountNumber(),
                    create.getMasterAccountNumber())).size() != 3) {

                log.error("Account number is duplicate in list: {}",
                        List.of(create.getSenderAccountNumber(), create.getReceiverAccountNumber(),
                                create.getMasterAccountNumber()));
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.DUPLICATE_ACCOUNT_NUMBER,
                                null, LocaleContextHolder.getLocale()));
            }

            // Check amount
            if (create.getSenderAmount().add(create.getReceiverAmount()).add(create.getFee())
                    .compareTo(BigDecimal.ZERO) != 0) {

                log.error("Invalid total amount: {} + {} + {} != 0",
                        create.getSenderAmount(), create.getReceiverAmount(), create.getFee());
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.INVALID_TOTAL_AMOUNT,
                                null, LocaleContextHolder.getLocale()));
            }

            // Generate reference code
            String referenceCode = new ULID().nextULID();
            log.info("Reference code: {}", referenceCode);

            // Get sender account
            Optional<Account> sender = accountRepository.findByAccountNumberAndStatus
                    (create.getSenderAccountNumber(), true);

            // Check sender account
            if (sender.isPresent() && sender.get().getIsActive()) {

                // Get receiver account
                log.info("Account is exist with account number: {}", create.getSenderAccountNumber());
                Optional<Account> receiver = accountRepository.findByAccountNumberAndStatus
                        (create.getReceiverAccountNumber(), true);

                // Check receiver account
                if (receiver.isPresent() && receiver.get().getIsActive()) {

                    log.info("Account is exist with account number: {}", create.getReceiverAccountNumber());
                    if (create.getFee().compareTo(BigDecimal.ZERO) > 0) {

                        // Get master account
                        Optional<Account> master = accountRepository.findByAccountNumberAndStatus
                                (create.getMasterAccountNumber(), true);

                        // Check master account
                        if (master.isPresent() && master.get().getIsActive()) {

                            log.info("Account is exist with account number: {}", create.getMasterAccountNumber());
                            BigDecimal fee = create.getFee();

                            // Create master transaction
                            Transaction masterTransaction = Transaction.builder()
                                    .id(new ULID().nextULID())
                                    .account(master.get())
                                    .amount(fee)
                                    .previousBalance(master.get().getBalance())
                                    .currentBalance(master.get().getBalance().add(fee))
                                    .availableBalance(master.get().getAvailableBalance().add(fee))
                                    .referenceCode(referenceCode)
                                    .type(TransactionType.INTERNAL)
                                    .direction(Direction.RECEIVE)
                                    .note(create.getNote())
                                    .description(create.getDescription())
                                    .state(true)
                                    .status(true)
                                    .build();

                            log.info("Before (Account number: {})", create.getMasterAccountNumber());
                            log.info("Balance: {}", master.get().getBalance());
                            log.info("Available balance: {}", master.get().getAvailableBalance());

                            // Update master account balance
                            master.get().setLastTransactionDate(LocalDateTime.now());
                            master.get().setBalance(master.get().getBalance().add(fee));
                            master.get().setAvailableBalance(master.get().getAvailableBalance().add(fee));
                            master.get().setTotalIncome(master.get().getTotalIncome().add(fee));

                            log.info("After (Account number: {})", create.getMasterAccountNumber());
                            log.info("Balance: {}", master.get().getBalance());
                            log.info("Available balance: {}", master.get().getAvailableBalance());

                            transactionRepository.save(masterTransaction);
                        } else {

                            log.error("Invalid master account");
                            throw new InvalidParameterException(
                                    messageSource.getMessage(
                                            Constant.INVALID_MASTER_ACCOUNT, null, LocaleContextHolder.getLocale()));
                        }
                    }

                    BigDecimal senderAmount = create.getSenderAmount();
                    BigDecimal receiverAmount = create.getReceiverAmount();

                    BigDecimal senderBalance = sender.get().getAvailableBalance();
                    BigDecimal receiverBalance = receiver.get().getAvailableBalance();

                    // Check balance
                    if (senderBalance.compareTo(senderAmount.negate()) < 0) {

                        log.error("Insufficient account balance to complete transaction");
                        throw new InvalidParameterException(
                                messageSource.getMessage(
                                        Constant.INSUFFICIENT_BALANCE, null, LocaleContextHolder.getLocale()));
                    }

                    // Create sender transaction
                    Transaction senderTransaction = Transaction.builder()
                            .id(new ULID().nextULID())
                            .account(sender.get())
                            .amount(senderAmount)
                            .previousBalance(sender.get().getBalance())
                            .currentBalance(sender.get().getBalance().add(senderAmount))
                            .availableBalance(senderBalance.add(senderAmount))
                            .referenceCode(referenceCode)
                            .type(TransactionType.INTERNAL)
                            .direction(Direction.SEND)
                            .note(create.getNote())
                            .description(create.getDescription())
                            .state(true)
                            .status(true)
                            .build();

                    // Create receiver transaction
                    Transaction receiverTransaction = Transaction.builder()
                            .id(new ULID().nextULID())
                            .account(receiver.get())
                            .amount(receiverAmount)
                            .previousBalance(receiver.get().getBalance())
                            .currentBalance(receiver.get().getBalance().add(receiverAmount))
                            .availableBalance(receiverBalance.add(receiverAmount))
                            .referenceCode(referenceCode)
                            .type(TransactionType.INTERNAL)
                            .direction(Direction.RECEIVE)
                            .note(create.getNote())
                            .description(create.getDescription())
                            .state(true)
                            .status(true)
                            .build();

                    log.info("Before (Account number: {})", create.getSenderAccountNumber());
                    log.info("Balance: {}", sender.get().getBalance());
                    log.info("Available balance: {}", sender.get().getAvailableBalance());

                    // Update sender account balance
                    sender.get().setLastTransactionDate(LocalDateTime.now());
                    sender.get().setBalance(sender.get().getBalance().add(senderAmount));
                    sender.get().setAvailableBalance(senderBalance.add(senderAmount));
                    sender.get().setTotalExpenditure(sender.get().getTotalExpenditure().add(senderAmount));

                    log.info("After (Account number: {})", create.getSenderAccountNumber());
                    log.info("Balance: {}", sender.get().getBalance());
                    log.info("Available balance: {}", sender.get().getAvailableBalance());

                    log.info("==================================================================");

                    log.info("Before (Account number: {})", create.getReceiverAccountNumber());
                    log.info("Balance: {}", receiver.get().getBalance());
                    log.info("Available balance: {}", receiver.get().getAvailableBalance());

                    // Update receiver account balance
                    receiver.get().setLastTransactionDate(LocalDateTime.now());
                    receiver.get().setBalance(receiver.get().getBalance().add(receiverAmount));
                    receiver.get().setAvailableBalance(receiverBalance.add(receiverAmount));
                    receiver.get().setTotalIncome(receiver.get().getTotalIncome().add(receiverAmount));

                    log.info("After (Account number: {})", create.getReceiverAccountNumber());
                    log.info("Balance: {}", receiver.get().getBalance());
                    log.info("Available balance: {}", receiver.get().getAvailableBalance());

                    transactionRepository.save(receiverTransaction);

                    return transactionMapper.entityToDTO(transactionRepository.save(senderTransaction));

                } else {

                    log.error("Invalid receiver account");
                    throw new InvalidParameterException(
                            messageSource.getMessage(
                                    Constant.INVALID_RECEIVER_ACCOUNT, null, LocaleContextHolder.getLocale()));
                }
            } else {

                log.error("Invalid sender account");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_SENDER_ACCOUNT, null, LocaleContextHolder.getLocale()));
            }
        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.CREATE_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public TransactionCoreNapasDTO createSystem(CreateSystemTransactionDTO create) {

        try {

            // Check account number
            log.info("Entering createSystem with parameters: create = {}", create.toString());
            if (new HashSet<>(List.of(create.getCustomerAccountNumber(), create.getMasterAccountNumber())).size() != 2) {


                log.error("Account number is duplicate in list: {}",
                        List.of(create.getCustomerAccountNumber(), create.getMasterAccountNumber()));
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.DUPLICATE_ACCOUNT_NUMBER,
                                null, LocaleContextHolder.getLocale()));
            }

            if (create.getCustomerAmount().negate().compareTo(create.getMasterAmount()) != 0) {

                log.error("Invalid balanced transaction");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_BALANCED_TRANSACTION, null, LocaleContextHolder.getLocale()));
            } else if (create.getCustomerAmount().compareTo(BigDecimal.ZERO) == 0) {

                log.error("Invalid amount (max=1.000.000.000)");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_AMOUNT, null, LocaleContextHolder.getLocale()));
            }

            // Generate reference code
            String referenceCode = new ULID().nextULID();
            log.info("Reference code: {}", referenceCode);

            boolean isSender = create.getCustomerAmount().compareTo(BigDecimal.ZERO) < 0;

            // Get customer account
            Optional<Account> customer = accountRepository.findByAccountNumberAndStatus
                    (create.getCustomerAccountNumber(), true);

            // Check customer account
            if (customer.isPresent() && customer.get().getIsActive()) {

                // Get master account
                log.info("Account is exist with account number: {}", create.getCustomerAccountNumber());
                Optional<Account> master = accountRepository.findByAccountNumberAndStatus
                        (create.getMasterAccountNumber(), true);

                // Check master account
                if (master.isPresent() && master.get().getIsActive()) {

                    log.info("Account is exist with account number: {}", create.getMasterAccountNumber());

                    BigDecimal customerAmount = create.getCustomerAmount();
                    BigDecimal masterAmount = create.getMasterAmount();

                    BigDecimal customerBalance = customer.get().getAvailableBalance();
                    BigDecimal masterBalance = master.get().getAvailableBalance();

                    // Check balance
                    if (isSender && customerBalance.compareTo(customerAmount.negate()) < 0) {

                        log.error("Insufficient account balance to complete transaction");
                        throw new InvalidParameterException(
                                messageSource.getMessage(
                                        Constant.INSUFFICIENT_BALANCE, null, LocaleContextHolder.getLocale()));
                    } else if (masterBalance.compareTo(masterAmount.negate()) < 0) {

                        log.error("Insufficient master account balance to complete transaction");
                        throw new InvalidParameterException(
                                messageSource.getMessage(
                                        Constant.INSUFFICIENT_MASTER_BALANCE, null, LocaleContextHolder.getLocale()));
                    }

                    // Create customer transaction
                    Transaction customerTransaction = Transaction.builder()
                            .id(new ULID().nextULID())
                            .account(customer.get())
                            .amount(customerAmount)
                            .previousBalance(customer.get().getBalance())
                            .currentBalance(customer.get().getBalance().add(customerAmount))
                            .availableBalance(customerBalance.add(customerAmount))
                            .referenceCode(referenceCode)
                            .type(TransactionType.SYSTEM)
                            .direction(isSender ? Direction.SEND : Direction.RECEIVE)
                            .note(create.getNote())
                            .description(create.getDescription())
                            .state(true)
                            .status(true)
                            .build();

                    // Create master transaction
                    Transaction masterTransaction = Transaction.builder()
                            .id(new ULID().nextULID())
                            .account(master.get())
                            .amount(masterAmount)
                            .previousBalance(master.get().getBalance())
                            .currentBalance(master.get().getBalance().add(masterAmount))
                            .availableBalance(masterBalance.add(masterAmount))
                            .referenceCode(referenceCode)
                            .type(TransactionType.SYSTEM)
                            .direction(!isSender ? Direction.SEND : Direction.RECEIVE)
                            .note(create.getNote())
                            .description(create.getDescription())
                            .state(true)
                            .status(true)
                            .build();

                    log.info("Before (Account number: {})", create.getCustomerAccountNumber());
                    log.info("Balance: {}", customer.get().getBalance());
                    log.info("Available balance: {}", customer.get().getAvailableBalance());

                    // Update customer account balance
                    customer.get().setLastTransactionDate(LocalDateTime.now());
                    customer.get().setBalance(customer.get().getBalance().add(customerAmount));
                    customer.get().setAvailableBalance(customerBalance.add(customerAmount));

                    log.info("After (Account number: {})", create.getCustomerAccountNumber());
                    log.info("Balance: {}", customer.get().getBalance());
                    log.info("Available balance: {}", customer.get().getAvailableBalance());

                    log.info("==================================================================");

                    log.info("Before (Account number: {})", create.getMasterAccountNumber());
                    log.info("Balance: {}", master.get().getBalance());
                    log.info("Available balance: {}", master.get().getAvailableBalance());

                    // Update master account balance
                    master.get().setLastTransactionDate(LocalDateTime.now());
                    master.get().setBalance(master.get().getBalance().add(masterAmount));
                    master.get().setAvailableBalance(masterBalance.add(masterAmount));

                    log.info("After (Account number: {})", create.getMasterAccountNumber());
                    log.info("Balance: {}", master.get().getBalance());
                    log.info("Available balance: {}", master.get().getAvailableBalance());

                    if (isSender) {

                        customer.get().setTotalExpenditure(customer.get().getTotalExpenditure().add(customerAmount));
                        master.get().setTotalIncome(master.get().getTotalIncome().add(masterAmount));
                    } else {

                        customer.get().setTotalIncome(customer.get().getTotalIncome().add(customerAmount));
                        master.get().setTotalExpenditure(master.get().getTotalExpenditure().add(masterAmount));
                    }

                    transactionRepository.save(masterTransaction);

                    return transactionMapper.entityToDTO(transactionRepository.save(customerTransaction));

                } else {

                    log.error("Invalid master account");
                    throw new InvalidParameterException(
                            messageSource.getMessage(
                                    Constant.INVALID_MASTER_ACCOUNT, null, LocaleContextHolder.getLocale()));
                }
            } else {

                log.error(isSender ? "Invalid sender account" : "Invalid receiver account");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                isSender ? Constant.INVALID_SENDER_ACCOUNT : Constant.INVALID_RECEIVER_ACCOUNT,
                                null, LocaleContextHolder.getLocale()));
            }
        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.CREATE_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void rollback(CoreTransactionRollbackDTO rollback) throws GroupValidationException {

        // Check transaction
        log.info("Roll back transaction type: {}", rollback.getType());
        log.info("Entering rollback with parameters: rollback = {}", rollback);
        if (!transactionRepository.existsByReferenceCode(rollback.getReferenceCode())) {

            log.error("Invalid reference code");
            throw new InvalidParameterException(
                    messageSource.getMessage(Constant.INVALID_REFERENCE_CODE,
                            null, LocaleContextHolder.getLocale()));
        }

        switch (rollback.getType()) {
            case EXTERNAL -> {

                Set<ConstraintViolation<Object>> violations =
                        validator.validate(rollback, CoreTransactionRollbackDTO.ExternalValidationGroup.class);

                if (!violations.isEmpty()) {

                    log.error(violations.toString());
                    throw new GroupValidationException(violations);
                }
                rollbackExternal(rollback);
            }
            case INTERNAL -> {

                Set<ConstraintViolation<Object>> violations =
                        validator.validate(rollback, CoreTransactionRollbackDTO.InternalValidationGroup.class);

                if (!violations.isEmpty()) {

                    log.error(violations.toString());
                    throw new GroupValidationException(violations);
                }

                rollbackInternal(rollback);
            }
            case SYSTEM -> {

                Set<ConstraintViolation<Object>> violations =
                        validator.validate(rollback, CoreTransactionRollbackDTO.SystemValidationGroup.class);

                if (!violations.isEmpty()) {

                    log.error(violations.toString());
                    throw new GroupValidationException(violations);
                }

                rollbackSystem(rollback);
            }
            default -> throw new InvalidParameterException
                    (messageSource.getMessage(Constant.ROLLBACK_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    private void rollbackExternal(CoreTransactionRollbackDTO rollback) {

        try {

            // Check account number
            log.info("Entering rollbackExternal with parameters: rollback = {}", rollback);
            if (new HashSet<>(List.of(rollback.getCustomerAccountNumber(), rollback.getMasterAccountNumber())).size() != 2) {

                log.error("Account number is duplicate in list: {}",
                        List.of(rollback.getCustomerAccountNumber(), rollback.getMasterAccountNumber()));
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.DUPLICATE_ACCOUNT_NUMBER,
                                null, LocaleContextHolder.getLocale()));
            }

            // Check amount
            if (rollback.getCustomerAmount().compareTo(BigDecimal.ZERO) == 0) {

                log.error("Invalid amount (max=1.000.000.000)");
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.INVALID_AMOUNT, null, LocaleContextHolder.getLocale()));
            }

            // Get account
            Optional<Account> account = accountRepository.findByAccountNumberAndStatus
                    (rollback.getCustomerAccountNumber(), true);

            // Check account
            if (account.isPresent()) {

                log.info("Account is exist with account number: {}", rollback.getCustomerAccountNumber());
                if (rollback.getMasterAmount().compareTo(BigDecimal.ZERO) > 0) {

                    // Get master account
                    Optional<Account> master = accountRepository.findByAccountNumberAndStatus
                            (rollback.getMasterAccountNumber(), true);

                    if (master.isPresent()) {

                        // Get master rollback reference code
                        log.info("Account is exist with account number: {}", rollback.getMasterAccountNumber());
                        Transaction transRef = master.get().getTransactionList().stream()
                                .filter(t -> t.getReferenceCode().equals(rollback.getReferenceCode()))
                                .findFirst().orElse(Transaction.builder().build());
                        log.info("Reference code: {}", transRef.getReferenceCode());
                        BigDecimal masterAmount = rollback.getMasterAmount().negate();

                        // Create master rollback transaction
                        Transaction masterTransaction = Transaction.builder()
                                .id(new ULID().nextULID())
                                .account(master.get())
                                .amount(masterAmount)
                                .previousBalance(master.get().getBalance())
                                .currentBalance(master.get().getBalance().add(masterAmount))
                                .availableBalance(master.get().getAvailableBalance().add(masterAmount))
                                .referenceCode(transRef.getId())
                                .type(TransactionType.ROLLBACK)
                                .direction(Direction.SEND)
                                .note(ROLLBACK_MESSAGE + transRef.getId())
                                .description(ROLLBACK_MESSAGE + transRef.getId())
                                .state(true)
                                .status(true)
                                .build();

                        log.info("Before (Account number: {})", rollback.getMasterAccountNumber());
                        log.info("Balance: {}", master.get().getBalance());
                        log.info("Available balance: {}", master.get().getAvailableBalance());

                        // Update master account balance
                        master.get().setLastTransactionDate(LocalDateTime.now());
                        master.get().setBalance(master.get().getBalance().add(masterAmount));
                        master.get().setAvailableBalance(master.get().getAvailableBalance().add(masterAmount));
                        master.get().setTotalIncome(master.get().getTotalIncome().add(masterAmount));

                        log.info("After (Account number: {})", rollback.getMasterAccountNumber());
                        log.info("Balance: {}", master.get().getBalance());
                        log.info("Available balance: {}", master.get().getAvailableBalance());

                        transactionRepository.save(masterTransaction);

                    } else {

                        log.error("Invalid master account");
                        throw new InvalidParameterException(
                                messageSource.getMessage(
                                        Constant.INVALID_MASTER_ACCOUNT, null, LocaleContextHolder.getLocale()));
                    }
                }

                BigDecimal amount = rollback.getCustomerAmount().negate();
                boolean isSender = amount.compareTo(BigDecimal.ZERO) < 0;
                BigDecimal balance = account.get().getAvailableBalance();

                // Get account rollback reference code
                Transaction transRef = account.get().getTransactionList().stream()
                        .filter(t -> t.getReferenceCode().equals(rollback.getReferenceCode()))
                        .findFirst().orElse(Transaction.builder().build());
                log.info("Reference code: {}", transRef.getReferenceCode());

                // Create rollback transaction
                Transaction transaction = Transaction.builder()
                        .id(new ULID().nextULID())
                        .account(account.get())
                        .amount(amount)
                        .previousBalance(account.get().getBalance())
                        .currentBalance(account.get().getBalance().add(amount))
                        .availableBalance(balance.add(amount))
                        .referenceCode(transRef.getId())
                        .type(TransactionType.ROLLBACK)
                        .direction(isSender ? Direction.SEND : Direction.RECEIVE)
                        .note(ROLLBACK_MESSAGE + transRef.getId())
                        .description(ROLLBACK_MESSAGE + transRef.getId())
                        .state(true)
                        .status(true)
                        .build();

                log.info("Before (Account number: {})", rollback.getCustomerAccountNumber());
                log.info("Balance: {}", account.get().getBalance());
                log.info("Available balance: {}", account.get().getAvailableBalance());

                // Update account balance
                account.get().setLastTransactionDate(LocalDateTime.now());
                account.get().setBalance(account.get().getBalance().add(amount));
                account.get().setAvailableBalance(balance.add(amount));

                log.info("After (Account number: {})", rollback.getCustomerAccountNumber());
                log.info("Balance: {}", account.get().getBalance());
                log.info("Available balance: {}", account.get().getAvailableBalance());

                if (!isSender) {

                    account.get().setTotalExpenditure(account.get().getTotalExpenditure().add(amount));
                } else {

                    account.get().setTotalIncome(account.get().getTotalIncome().add(amount));
                }

                transactionRepository.save(transaction);
            } else {

                log.error("Invalid account");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_ACCOUNT, null, LocaleContextHolder.getLocale()));
            }
        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.ROLLBACK_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    private void rollbackInternal(CoreTransactionRollbackDTO rollback) {

        try {

            // Check account number
            log.info("Entering rollbackInternal with parameters: rollback = {}", rollback);
            if (new HashSet<>(List.of(rollback.getSenderAccountNumber(), rollback.getReceiverAccountNumber(),
                    rollback.getMasterAccountNumber())).size() != 3) {

                log.error("Account number is duplicate in list: {}",
                        List.of(rollback.getSenderAccountNumber(), rollback.getReceiverAccountNumber(),
                                rollback.getMasterAccountNumber()));
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.DUPLICATE_ACCOUNT_NUMBER,
                                null, LocaleContextHolder.getLocale()));
            }

            // Check amount
            if (rollback.getSenderAmount().add(rollback.getReceiverAmount()).add(rollback.getMasterAmount())
                    .compareTo(BigDecimal.ZERO) != 0) {

                log.error("Invalid total amount: {} + {} + {} != 0",
                        rollback.getSenderAmount(), rollback.getReceiverAmount(), rollback.getMasterAmount());
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.INVALID_TOTAL_AMOUNT,
                                null, LocaleContextHolder.getLocale()));
            }

            // Get sender account
            Optional<Account> sender = accountRepository.findByAccountNumberAndStatus
                    (rollback.getSenderAccountNumber(), true);

            // Check sender account
            if (sender.isPresent()) {

                // Get receiver account
                log.info("Account is exist with account number: {}", rollback.getSenderAccountNumber());
                Optional<Account> receiver = accountRepository.findByAccountNumberAndStatus
                        (rollback.getReceiverAccountNumber(), true);

                // Check receiver account
                if (receiver.isPresent()) {

                    log.info("Account is exist with account number: {}", rollback.getReceiverAccountNumber());
                    if (rollback.getMasterAmount().compareTo(BigDecimal.ZERO) > 0) {

                        // Get master account
                        Optional<Account> master = accountRepository.findByAccountNumberAndStatus
                                (rollback.getMasterAccountNumber(), true);

                        // Check master account
                        if (master.isPresent()) {

                            // Get master rollback reference code
                            log.info("Account is exist with account number: {}", rollback.getMasterAccountNumber());
                            Transaction transRef = master.get().getTransactionList().stream()
                                    .filter(t -> t.getReferenceCode().equals(rollback.getReferenceCode()))
                                    .findFirst().orElse(Transaction.builder().build());
                            BigDecimal masterAmount = rollback.getMasterAmount().negate();

                            // Create master rollback transaction
                            Transaction masterTransaction = Transaction.builder()
                                    .id(new ULID().nextULID())
                                    .account(master.get())
                                    .amount(masterAmount)
                                    .previousBalance(master.get().getBalance())
                                    .currentBalance(master.get().getBalance().add(masterAmount))
                                    .availableBalance(master.get().getAvailableBalance().add(masterAmount))
                                    .referenceCode(transRef.getId())
                                    .type(TransactionType.ROLLBACK)
                                    .direction(Direction.SEND)
                                    .note(ROLLBACK_MESSAGE + transRef.getId())
                                    .description(ROLLBACK_MESSAGE + transRef.getId())
                                    .state(true)
                                    .status(true)
                                    .build();

                            log.info("Before (Account number: {})", rollback.getMasterAccountNumber());
                            log.info("Balance: {}", master.get().getBalance());
                            log.info("Available balance: {}", master.get().getAvailableBalance());

                            // Update master account balance
                            master.get().setLastTransactionDate(LocalDateTime.now());
                            master.get().setBalance(master.get().getBalance().add(masterAmount));
                            master.get().setAvailableBalance(master.get().getAvailableBalance().add(masterAmount));
                            master.get().setTotalIncome(master.get().getTotalIncome().add(masterAmount));

                            log.info("After (Account number: {})", rollback.getMasterAccountNumber());
                            log.info("Balance: {}", master.get().getBalance());
                            log.info("Available balance: {}", master.get().getAvailableBalance());

                            transactionRepository.save(masterTransaction);
                        } else {

                            log.error("Invalid master account");
                            throw new InvalidParameterException(
                                    messageSource.getMessage(
                                            Constant.INVALID_MASTER_ACCOUNT, null, LocaleContextHolder.getLocale()));
                        }
                    }

                    BigDecimal senderAmount = rollback.getSenderAmount().negate();
                    BigDecimal receiverAmount = rollback.getReceiverAmount().negate();

                    BigDecimal senderBalance = sender.get().getAvailableBalance();
                    BigDecimal receiverBalance = receiver.get().getAvailableBalance();

                    // Get sender rollback reference code
                    Transaction transRef = sender.get().getTransactionList().stream()
                            .filter(t -> t.getReferenceCode().equals(rollback.getReferenceCode()))
                            .findFirst().orElse(Transaction.builder().build());
                    log.info("Reference code: {}", transRef.getReferenceCode());

                    // Create sender rollback transaction
                    Transaction senderTransaction = Transaction.builder()
                            .id(new ULID().nextULID())
                            .account(sender.get())
                            .amount(senderAmount)
                            .previousBalance(sender.get().getBalance())
                            .currentBalance(sender.get().getBalance().add(senderAmount))
                            .availableBalance(senderBalance.add(senderAmount))
                            .referenceCode(transRef.getId())
                            .type(TransactionType.ROLLBACK)
                            .direction(Direction.RECEIVE)
                            .note(ROLLBACK_MESSAGE + transRef.getId())
                            .description(ROLLBACK_MESSAGE + transRef.getId())
                            .state(true)
                            .status(true)
                            .build();

                    // Get receiver rollback reference code
                    Transaction transRefReceiver = receiver.get().getTransactionList().stream()
                            .filter(t -> t.getReferenceCode().equals(rollback.getReferenceCode()))
                            .findFirst().orElse(Transaction.builder().build());

                    // Create receiver rollback transaction
                    Transaction receiverTransaction = Transaction.builder()
                            .id(new ULID().nextULID())
                            .account(receiver.get())
                            .amount(receiverAmount)
                            .previousBalance(receiver.get().getBalance())
                            .currentBalance(receiver.get().getBalance().add(receiverAmount))
                            .availableBalance(receiverBalance.add(receiverAmount))
                            .referenceCode(transRefReceiver.getId())
                            .type(TransactionType.ROLLBACK)
                            .direction(Direction.SEND)
                            .note(ROLLBACK_MESSAGE + transRefReceiver.getId())
                            .description(ROLLBACK_MESSAGE + transRefReceiver.getId())
                            .state(true)
                            .status(true)
                            .build();

                    log.info("Before (Account number: {})", rollback.getSenderAccountNumber());
                    log.info("Balance: {}", sender.get().getBalance());
                    log.info("Available balance: {}", sender.get().getAvailableBalance());

                    // Update sender account balance
                    sender.get().setLastTransactionDate(LocalDateTime.now());
                    sender.get().setBalance(sender.get().getBalance().add(senderAmount));
                    sender.get().setAvailableBalance(senderBalance.add(senderAmount));
                    sender.get().setTotalExpenditure(sender.get().getTotalExpenditure().add(senderAmount));

                    log.info("After (Account number: {})", rollback.getSenderAccountNumber());
                    log.info("Balance: {}", sender.get().getBalance());
                    log.info("Available balance: {}", sender.get().getAvailableBalance());

                    log.info("==================================================================");

                    log.info("Before (Account number: {})", rollback.getReceiverAccountNumber());
                    log.info("Balance: {}", receiver.get().getBalance());
                    log.info("Available balance: {}", receiver.get().getAvailableBalance());

                    // Update receiver account balance
                    receiver.get().setLastTransactionDate(LocalDateTime.now());
                    receiver.get().setBalance(receiver.get().getBalance().add(receiverAmount));
                    receiver.get().setAvailableBalance(receiverBalance.add(receiverAmount));
                    receiver.get().setTotalIncome(receiver.get().getTotalIncome().add(receiverAmount));

                    log.info("After (Account number: {})", rollback.getReceiverAccountNumber());
                    log.info("Balance: {}", receiver.get().getBalance());
                    log.info("Available balance: {}", receiver.get().getAvailableBalance());

                    transactionRepository.saveAll(List.of(receiverTransaction, senderTransaction));
                }
            }
        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.ROLLBACK_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    private void rollbackSystem(CoreTransactionRollbackDTO rollback) {

        try {

            // Check account number
            log.info("Entering rollbackSystem with parameters: rollback = {}", rollback);
            if (new HashSet<>(List.of(rollback.getCustomerAccountNumber(), rollback.getMasterAccountNumber())).size() != 2) {

                log.error("Account number is duplicate in list: {}",
                        List.of(rollback.getCustomerAccountNumber(), rollback.getMasterAccountNumber()));
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.DUPLICATE_ACCOUNT_NUMBER,
                                null, LocaleContextHolder.getLocale()));
            }

            if (rollback.getCustomerAmount().negate().compareTo(rollback.getMasterAmount()) != 0) {

                log.error("Invalid balanced transaction");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_BALANCED_TRANSACTION, null, LocaleContextHolder.getLocale()));
            } else if (rollback.getCustomerAmount().compareTo(BigDecimal.ZERO) == 0) {

                log.error("Customer amount = 0");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_AMOUNT, null, LocaleContextHolder.getLocale()));
            }

            boolean isSender = rollback.getCustomerAmount().negate().compareTo(BigDecimal.ZERO) < 0;

            // Get customer account
            Optional<Account> customer = accountRepository.findByAccountNumberAndStatus
                    (rollback.getCustomerAccountNumber(), true);

            // Check customer account
            if (customer.isPresent()) {

                // Get master account
                log.info("Account is exist with account number: {}", rollback.getCustomerAccountNumber());
                Optional<Account> master = accountRepository.findByAccountNumberAndStatus
                        (rollback.getMasterAccountNumber(), true);

                // Check master account
                if (master.isPresent()) {

                    log.info("Account is exist with account number: {}", rollback.getMasterAccountNumber());
                    BigDecimal customerAmount = rollback.getCustomerAmount().negate();
                    BigDecimal masterAmount = rollback.getMasterAmount().negate();

                    BigDecimal customerBalance = customer.get().getAvailableBalance();
                    BigDecimal masterBalance = master.get().getAvailableBalance();

                    // Get customer rollback reference code
                    Transaction transRef = customer.get().getTransactionList().stream()
                            .filter(t -> t.getReferenceCode().equals(rollback.getReferenceCode()))
                            .findFirst().orElse(Transaction.builder().build());
                    log.info("Reference code: {}", transRef.getReferenceCode());

                    // Create customer rollback transaction
                    Transaction customerTransaction = Transaction.builder()
                            .id(new ULID().nextULID())
                            .account(customer.get())
                            .amount(customerAmount)
                            .previousBalance(customer.get().getBalance())
                            .currentBalance(customer.get().getBalance().add(customerAmount))
                            .availableBalance(customerBalance.add(customerAmount))
                            .referenceCode(transRef.getId())
                            .type(TransactionType.ROLLBACK)
                            .direction(isSender ? Direction.SEND : Direction.RECEIVE)
                            .note(ROLLBACK_MESSAGE + transRef.getId())
                            .description(ROLLBACK_MESSAGE + transRef.getId())
                            .state(true)
                            .status(true)
                            .build();

                    // Get master rollback reference code
                    Transaction transRefMaster = master.get().getTransactionList().stream()
                            .filter(t -> t.getReferenceCode().equals(rollback.getReferenceCode()))
                            .findFirst().orElse(Transaction.builder().build());

                    // Create master rollback transaction
                    Transaction masterTransaction = Transaction.builder()
                            .id(new ULID().nextULID())
                            .account(master.get())
                            .amount(masterAmount)
                            .previousBalance(master.get().getBalance())
                            .currentBalance(master.get().getBalance().add(masterAmount))
                            .availableBalance(masterBalance.add(masterAmount))
                            .referenceCode(transRefMaster.getId())
                            .type(TransactionType.ROLLBACK)
                            .direction(!isSender ? Direction.SEND : Direction.RECEIVE)
                            .note(ROLLBACK_MESSAGE + transRefMaster.getId())
                            .description(ROLLBACK_MESSAGE + transRefMaster.getId())
                            .state(true)
                            .status(true)
                            .build();

                    log.info("Before (Account number: {})", rollback.getCustomerAccountNumber());
                    log.info("Balance: {}", customer.get().getBalance());
                    log.info("Available balance: {}", customer.get().getAvailableBalance());

                    // Update customer account balance
                    customer.get().setLastTransactionDate(LocalDateTime.now());
                    customer.get().setBalance(customer.get().getBalance().add(customerAmount));
                    customer.get().setAvailableBalance(customerBalance.add(customerAmount));

                    log.info("After (Account number: {})", rollback.getCustomerAccountNumber());
                    log.info("Balance: {}", customer.get().getBalance());
                    log.info("Available balance: {}", customer.get().getAvailableBalance());

                    log.info("==================================================================");

                    log.info("Before (Account number: {})", rollback.getMasterAccountNumber());
                    log.info("Balance: {}", master.get().getBalance());
                    log.info("Available balance: {}", master.get().getAvailableBalance());

                    // Update master account balance
                    master.get().setLastTransactionDate(LocalDateTime.now());
                    master.get().setBalance(master.get().getBalance().add(masterAmount));
                    master.get().setAvailableBalance(masterBalance.add(masterAmount));

                    log.info("After (Account number: {})", rollback.getMasterAccountNumber());
                    log.info("Balance: {}", master.get().getBalance());
                    log.info("Available balance: {}", master.get().getAvailableBalance());

                    if (!isSender) {

                        customer.get().setTotalExpenditure(customer.get().getTotalExpenditure().add(customerAmount));
                        master.get().setTotalIncome(master.get().getTotalIncome().add(masterAmount));
                    } else {

                        customer.get().setTotalIncome(customer.get().getTotalIncome().add(customerAmount));
                        master.get().setTotalExpenditure(master.get().getTotalExpenditure().add(masterAmount));
                    }

                    transactionRepository.saveAll(List.of(masterTransaction, customerTransaction));
                } else {

                    log.error("Invalid master account");
                    throw new InvalidParameterException(
                            messageSource.getMessage(
                                    Constant.INVALID_MASTER_ACCOUNT, null, LocaleContextHolder.getLocale()));
                }
            } else {

                log.error(isSender ? "Invalid sender account" : "Invalid receiver account");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                isSender ? Constant.INVALID_SENDER_ACCOUNT : Constant.INVALID_RECEIVER_ACCOUNT,
                                null, LocaleContextHolder.getLocale()));
            }
        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.ROLLBACK_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }
}
