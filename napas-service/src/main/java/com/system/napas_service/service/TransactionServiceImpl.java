package com.system.napas_service.service;

import com.system.common_library.dto.request.transaction.CreateNapasTransactionDTO;
import com.system.common_library.dto.request.transaction.NapasTransactionRollbackDTO;
import com.system.common_library.dto.response.transaction.TransactionCoreNapasDTO;
import com.system.common_library.enums.Direction;
import com.system.napas_service.entity.Account;
import com.system.napas_service.entity.Transaction;
import com.system.napas_service.mapper.TransactionMapper;
import com.system.napas_service.repository.AccountRepository;
import com.system.napas_service.repository.BankRepository;
import com.system.napas_service.repository.TransactionRepository;
import com.system.napas_service.service.interfaces.TransactionService;
import com.system.napas_service.util.Constant;
import de.huxhorn.sulky.ulid.ULID;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final MessageSource messageSource;

    private final TransactionMapper transactionMapper;

    private final TransactionRepository transactionRepository;

    private final BankRepository bankRepository;

    private final AccountRepository accountRepository;

    private static final String ROLLBACK_MESSAGE = "Rollback transaction id : ";

    @Override
    public TransactionCoreNapasDTO create(CreateNapasTransactionDTO create) {

        try {

            log.info("Entering createExternal with parameters: create = {}", create.toString());
            if (!bankRepository.existsByNapasCode(create.getNapasCode())) {

                log.error("Invalid napas code");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_NAPAS_CODE, null, LocaleContextHolder.getLocale()));
            }

            // Get receiver account
            Optional<Account> account = accountRepository.findByAccountNumberAndStatus
                    (create.getReceiverAccount(), true);

            // Check receiver account
            if (account.isPresent() && account.get().getIsActive()
                    && account.get().getBank().getNapasCode().equals(create.getNapasCode())) {

                log.info("Account is exist with account number: {}", create.getReceiverAccount());
                BigDecimal amount = create.getAmount();
                BigDecimal balance = account.get().getBalance();
                BigDecimal availableBalance = account.get().getAvailableBalance();

                Transaction transaction = Transaction.builder()
                        .id(new ULID().nextULID())
                        .account(account.get())
                        .napasCode(create.getNapasCode())
                        .senderAccount(create.getSenderAccount())
                        .receiverAccount(create.getReceiverAccount())
                        .amount(amount)
                        .previousBalance(balance)
                        .currentBalance(balance.add(amount))
                        .availableBalance(availableBalance.add(amount))
                        .referenceCode(null)
                        .direction(Direction.RECEIVE)
                        .note(create.getNote())
                        .description(create.getDescription())
                        .state(true)
                        .status(true)
                        .build();

                log.info("Before (Account number: {})", create.getReceiverAccount());
                log.info("Balance: {}", account.get().getBalance());
                log.info("Available balance: {}", account.get().getAvailableBalance());

                account.get().setLastTransactionDate(LocalDateTime.now());
                account.get().setBalance(balance.add(amount));
                account.get().setAvailableBalance(availableBalance.add(amount));
                account.get().setTotalIncome(account.get().getTotalIncome().add(amount));

                log.info("After (Account number: {})", create.getReceiverAccount());
                log.info("Balance: {}", account.get().getBalance());
                log.info("Available balance: {}", account.get().getAvailableBalance());

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
    public void rollback(NapasTransactionRollbackDTO rollback) {

        try {

            log.info("Entering rollback with parameters: rollback = {}", rollback);
            Optional<Transaction> transactionRef = transactionRepository.findById(rollback.getTransactionId());

            if (transactionRef.isPresent()) {

                log.info("Transaction is exist with id: {}", rollback.getTransactionId());
                Optional<Account> account = accountRepository.findByAccountNumberAndStatus
                        (rollback.getReceiverAccount(), true);

                if (account.isPresent()) {

                    log.info("Account is exist with account number: {}", rollback.getReceiverAccount());
                    BigDecimal amount = rollback.getAmount().negate();
                    BigDecimal balance = account.get().getBalance();
                    BigDecimal availableBalance = account.get().getAvailableBalance();

                    Transaction transaction = Transaction.builder()
                            .id(new ULID().nextULID())
                            .account(account.get())
                            .napasCode(transactionRef.get().getNapasCode())
                            .senderAccount(rollback.getSenderAccount())
                            .receiverAccount(rollback.getReceiverAccount())
                            .amount(amount)
                            .previousBalance(balance)
                            .currentBalance(balance.add(amount))
                            .availableBalance(availableBalance.add(amount))
                            .referenceCode(transactionRef.get().getId())
                            .direction(Direction.SEND)
                            .note(ROLLBACK_MESSAGE + transactionRef.get().getId())
                            .description(ROLLBACK_MESSAGE + transactionRef.get().getId())
                            .state(true)
                            .status(true)
                            .build();

                    log.info("Before (Account number: {})", rollback.getReceiverAccount());
                    log.info("Balance: {}", account.get().getBalance());
                    log.info("Available balance: {}", account.get().getAvailableBalance());

                    account.get().setLastTransactionDate(LocalDateTime.now());
                    account.get().setBalance(balance.add(amount));
                    account.get().setAvailableBalance(availableBalance.add(amount));
                    account.get().setTotalIncome(account.get().getTotalIncome().add(amount));

                    log.info("After (Account number: {})", rollback.getReceiverAccount());
                    log.info("Balance: {}", account.get().getBalance());
                    log.info("Available balance: {}", account.get().getAvailableBalance());

                    transactionRepository.save(transaction);
                } else {

                    log.error("Invalid account");
                    throw new InvalidParameterException(
                            messageSource.getMessage(
                                    Constant.INVALID_ACCOUNT, null, LocaleContextHolder.getLocale()));
                }
            } else {

                log.error("Invalid transaction id");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_TRANSACTION_ID, null, LocaleContextHolder.getLocale()));
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
