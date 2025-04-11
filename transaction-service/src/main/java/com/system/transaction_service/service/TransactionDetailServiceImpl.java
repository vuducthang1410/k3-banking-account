package com.system.transaction_service.service;

import com.system.common_library.dto.notifcation.OTP;
import com.system.common_library.dto.request.transaction.*;
import com.system.common_library.dto.response.account.AccountExtraCoreDTO;
import com.system.common_library.dto.response.account.AccountExtraNapasDTO;
import com.system.common_library.dto.response.account.AccountInfoDTO;
import com.system.common_library.dto.response.transaction.TransactionCoreNapasDTO;
import com.system.common_library.dto.response.transaction.TransactionNotificationDTO;
import com.system.common_library.dto.transaction.*;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.*;
import com.system.common_library.exception.DubboException;
import com.system.common_library.service.AccountDubboService;
import com.system.common_library.service.CustomerDubboService;
import com.system.common_library.service.NotificationDubboService;
import com.system.transaction_service.client.core.AccountCoreFeignClient;
import com.system.transaction_service.client.core.TransactionCoreFeignClient;
import com.system.transaction_service.client.napas.AccountNapasFeignClient;
import com.system.transaction_service.client.napas.TransactionNapasFeignClient;
import com.system.transaction_service.config.VaultConfig;
import com.system.transaction_service.dto.projection.TransactionHistoryProjection;
import com.system.transaction_service.dto.request.OTPRequestDTO;
import com.system.transaction_service.dto.response.PagedDTO;
import com.system.transaction_service.dto.response.TransactionHistoryRp;
import com.system.transaction_service.entity.*;
import com.system.transaction_service.mapper.MapToDto;
import com.system.transaction_service.mapper.TransactionDetailMapper;
import com.system.transaction_service.repository.*;
import com.system.transaction_service.service.interfaces.NotificationService;
import com.system.transaction_service.service.interfaces.PagingService;
import com.system.transaction_service.service.interfaces.TransactionDetailService;
import com.system.transaction_service.util.Constant;
import com.system.transaction_service.util.OTPGenerator;
import de.huxhorn.sulky.ulid.ULID;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TransactionDetailServiceImpl implements TransactionDetailService {

    private static final String ROLLBACK_MESSAGE = "Rollback transaction id : ";

    private final MessageSource messageSource;

    private final VaultConfig vaultConfig;

    private final RedisTemplate<String, Object> redisTemplate;

    private final TransactionDetailMapper transactionDetailMapper;

    private final PagingService pagingService;

    private final TransactionDetailRepository transactionDetailRepository;

    private final TransactionRepository transactionRepository;

    private final ExternalBankRepository externalBankRepository;

    private final ExternalTransactionRepository externalTransactionRepository;

    private final InternalTransactionRepository internalTransactionRepository;

    private final PaymentTransactionRepository paymentTransactionRepository;

    private final TransactionStateRepository transactionStateRepository;

    private final NotificationService notificationService;

    private final AccountCoreFeignClient accountCoreFeignClient;

    private final AccountNapasFeignClient accountNapasFeignClient;

    private final TransactionCoreFeignClient transactionCoreFeignClient;

    private final TransactionNapasFeignClient transactionNapasFeignClient;

    @DubboReference
    private final AccountDubboService accountDubboService;

    @DubboReference
    private final CustomerDubboService customerDubboService;
    @DubboReference
    private NotificationDubboService notificationDubboService;

    @Override
    public TransactionExtraDTO findById(String id) {

        log.info("Entering findByAccountNumber with parameters: id = {}", id);
        Optional<TransactionDetail> transaction = transactionDetailRepository.findByIdAndStatus(id, true);

        return transaction.map(transactionDetailMapper::entityToExtraDTO).orElse(null);
    }

    @Override
    public PagedDTO<TransactionDTO> findAllByCondition(
            List<Direction> directionList, List<FeePayer> feePayerList, List<Initiator> initiatorList,
            List<Method> methodList, List<TransactionType> transactionTypeList, List<State> stateList, List<Type> typeList,
            String search, BigDecimal amountStart, BigDecimal amountEnd, String sort, int page, int limit) {

        log.info("Entering findAllByCondition with parameters: " +
                        "directionList = {}, feePayerList = {}, initiatorList = {}, methodList = {}, " +
                        "transactionTypeList = {}, typeList = {}, search = {}, amountStart = {}, amountEnd = {}, " +
                        "sort = {}, page = {}, limit = {}",
                directionList, feePayerList, initiatorList, methodList, transactionTypeList, typeList, search,
                amountStart, amountEnd, sort, page, limit);
        Pageable pageable = pagingService.getPageable(sort, page, limit, TransactionDetail.class);

        Page<TransactionDetail> pageResult = transactionDetailRepository.findAllByCondition(
                true, directionList, feePayerList, initiatorList, methodList, transactionTypeList, stateList,
                typeList, typeList.contains(Type.TRANSFER), search, amountStart, amountEnd, pageable);

        return new PagedDTO<>(pageResult.map(transactionDetailMapper::entityToDTO));
    }

    @Override
    public TransactionInitDTO createExternal(CreateExternalDTO create) {

        // Check account number
        log.info("Entering createExternal with parameters: create = {}", create.toString());
        if (new HashSet<>(List.of(create.getSenderAccount(), create.getReceiverAccount())).size() != 2) {

            log.error("Account number is duplicate in list: {}",
                    List.of(create.getSenderAccount(), create.getReceiverAccount()));
            throw new InvalidParameterException(
                    messageSource.getMessage(Constant.DUPLICATE_ACCOUNT_NUMBER,
                            null, LocaleContextHolder.getLocale()));
        }

        // Check customer validation (Customer service)
        try {
            CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(create.getCifCode());

            if (customer == null || !customer.isActive())
                throw new Exception();

            log.info("Customer information with CIF code({}): {}", create.getCifCode(), customer);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid customer");
            throw new InvalidParameterException(messageSource.getMessage
                    (Constant.INVALID_CUSTOMER, null, LocaleContextHolder.getLocale()));
        }

        if (!List.of(AccountType.PAYMENT, AccountType.CREDIT).contains(create.getSenderAccountType())) {

            log.error("Invalid account type");
            throw new InvalidParameterException(messageSource.getMessage
                    (Constant.INVALID_ACCOUNT_TYPE, null, LocaleContextHolder.getLocale()));
        }

        // Check sender account validation (Account service)
        AccountInfoDTO senderAccount;
        try {

            senderAccount = accountDubboService.getAccountDetail(create.getSenderAccount());

            if (senderAccount == null || !senderAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Sender account information with account number({}): {}", create.getSenderAccount(), senderAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid sender account");
            throw new InvalidParameterException(
                    messageSource.getMessage(
                            Constant.INVALID_SENDER_ACCOUNT, null, LocaleContextHolder.getLocale()));
        }

        // Check receiver account validation (Napas)
        ResponseEntity<AccountExtraNapasDTO> rsReceiverAccount = accountNapasFeignClient.getByAccountNumber
                (create.getReceiverAccount());
        AccountExtraNapasDTO receiverAccount = rsReceiverAccount.getBody();
        if (receiverAccount == null || !receiverAccount.getIsActive()) {

            log.error("Invalid receiver account");
            throw new InvalidParameterException(
                    messageSource.getMessage(
                            Constant.INVALID_RECEIVER_ACCOUNT, null, LocaleContextHolder.getLocale()));
        }
        log.info("Receiver account information with account({}): {}", create.getReceiverAccount(), receiverAccount);

        // Check master account (Core banking)
        boolean isPayFee = create.getFee().compareTo(BigDecimal.ZERO) > 0;
        AccountExtraCoreDTO masterAccount = AccountExtraCoreDTO.builder().build();
        if (isPayFee) {

            ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
                    (vaultConfig.getAccountNumber());
            masterAccount = rsMasterAccount.getBody();
            if (masterAccount == null || !masterAccount.getIsActive()) {

                log.error("Invalid master account");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_MASTER_ACCOUNT, null, LocaleContextHolder.getLocale()));
            }
            log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);
        }

        try {

            // Check napas code
            if (!create.getNapasCode().equals(receiverAccount.getBankNapasCode())) {

                log.error("Invalid napas code");
                throw new InvalidParameterException(messageSource.getMessage
                        (Constant.INVALID_NAPAS_CODE, null, LocaleContextHolder.getLocale()));
            }

            // Create external transaction
            ExternalTransaction transaction = transactionDetailMapper.createToExternalEntity(create);
            transaction.setExternalBank(
                    externalBankRepository.findByNapasCodeAndStatus(create.getNapasCode(), true)
                            .orElseThrow(()
                                    -> new InvalidParameterException(
                                    messageSource.getMessage(Constant.INVALID_NAPAS_CODE,
                                            null, LocaleContextHolder.getLocale()))));

            // Create transaction detail
            BigDecimal amount = create.getAmount().negate();
            log.info("Amount: {}", amount);

            BigDecimal fee = create.getFeePayer().equals(FeePayer.SENDER) ? create.getFee() : BigDecimal.ZERO;
            log.info("Fee: {}", fee);

            BigDecimal netAmount = amount.add(fee.negate());
            log.info("Net amount: {}", netAmount);

            if (senderAccount.getCurrentAccountBalance().compareTo(netAmount.abs()) < 0) {

                log.error("Insufficient account balance to complete transaction");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INSUFFICIENT_BALANCE, null, LocaleContextHolder.getLocale()));
            }

            List<TransactionDetail> detailList = new ArrayList<>(List.of(
                    TransactionDetail.builder()
                            .id(new ULID().nextULID())
                            .transaction(transaction)
                            .customerId(senderAccount.getCustomerId())
                            .account(create.getSenderAccount())
                            .amount(amount)
                            .fee(fee)
                            .netAmount(netAmount)
                            .previousBalance(senderAccount.getCurrentAccountBalance())
                            .currentBalance(senderAccount.getCurrentAccountBalance().add(netAmount))
                            .availableBalance(senderAccount.getCurrentAccountBalance().add(netAmount))
                            .direction(Direction.SEND)
                            .description(create.getDescription())
                            .status(true)
                            .build()));

            if (isPayFee) {

                detailList.add(TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(masterAccount.getCustomerId())
                        .account(vaultConfig.getAccountNumber())
                        .amount(create.getFee())
                        .fee(BigDecimal.ZERO)
                        .netAmount(create.getFee())
                        .previousBalance(masterAccount.getBalance())
                        .currentBalance(masterAccount.getBalance().add(create.getFee()))
                        .availableBalance(masterAccount.getAvailableBalance().add(create.getFee()))
                        .direction(Direction.RECEIVE)
                        .description(create.getDescription())
                        .status(true)
                        .build());
            }

            // Set transaction detail
            transaction.setTransactionDetailList(detailList);

            // Set transaction state
            List<TransactionState> stateList = List.of(
                    TransactionState.builder()
                            .id(new ULID().nextULID())
                            .transaction(transaction)
                            .state(State.PENDING)
                            .description(State.PENDING.getDescription())
                            .status(true)
                            .build());
            transaction.setTransactionStateList(stateList);

            // Save transaction
            log.info("Transaction id: {}", transaction.getId());
            ExternalTransaction externalTransaction = transactionRepository.save(transaction);

            // Send OTP code to email (Notification service)
            notificationService.sendOtpCode(OTPRequestDTO.builder()
                    .email(create.getEmail())
                    .transactionId(transaction.getId())
                    .build());

            log.info("Init external transaction successful");
            return transactionDetailMapper.externalEntityToInit(externalTransaction);
        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.CREATE_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public TransactionExtraDTO confirmExternal(String transactionId, String otp) {

        log.info("Entering confirmExternal with parameters: transactionId = {}, otp = {}", transactionId, otp);
        ExternalTransaction externalTransaction;
        try {

            // Check transaction ID
            externalTransaction = externalTransactionRepository.findById(transactionId).orElse(null);
            if (externalTransaction == null) {

                log.error("Invalid transaction id");
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.INVALID_TRANSACTION_ID, null, LocaleContextHolder.getLocale()));
            } else if (externalTransaction.getTransactionStateList().size() > 1) {

                log.error("Invalid transaction (invalid state)");
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.INVALID_TRANSACTION, null, LocaleContextHolder.getLocale()));
            }

            // Check OTP
            String key = Constant.CACHE_TRANSACTION_PREFIX + "otp:" + transactionId;
            Object otpFromRedis = redisTemplate.opsForValue().get(key);

            if (otpFromRedis == null || !otpFromRedis.toString().equals(otp)) {

                log.error("Invalid OTP code");
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.INVALID_OTP_CODE, null, LocaleContextHolder.getLocale()));
            }

        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.CREATE_FAIL, null, LocaleContextHolder.getLocale()));
        }

        // Set up data
        int count = 0;
        boolean isSuccess = false;
        String masterAccount = vaultConfig.getAccountNumber();
        // Core banking service
        TransactionCoreNapasDTO coreTrans = new TransactionCoreNapasDTO();
        log.info("Sender (Account number: {})", externalTransaction.getSenderAccount());
        BigDecimal amount = externalTransaction.getAmount().negate();
        log.info("Amount: {}", amount);
        BigDecimal fee = externalTransaction.getFeePayer().equals(FeePayer.SENDER) ?
                externalTransaction.getFee() : BigDecimal.ZERO;
        log.info("Fee: {}", fee);
        BigDecimal netAmount = amount.add(fee.negate());
        log.info("Net amount: {}", netAmount);

        // Napas service
        TransactionCoreNapasDTO napasTrans = new TransactionCoreNapasDTO();
        log.info("Receiver (Account number: {})", externalTransaction.getReceiverAccount());
        BigDecimal napasAmount = externalTransaction.getAmount();
        log.info("Napas amount: {}", napasAmount);
        BigDecimal napasFee = externalTransaction.getFeePayer().equals(FeePayer.RECEIVER) ?
                externalTransaction.getFee() : BigDecimal.ZERO;
        log.info("Napas fee: {}", napasFee);
        BigDecimal netNapasAmount = napasAmount.add(napasFee.negate());
        log.info("Net napas amount: {}", netNapasAmount);

        try {

            // Update sender/master account balance (Core banking)
            ResponseEntity<TransactionCoreNapasDTO> rsCoreTransaction = transactionCoreFeignClient.createExternal
                    (CreateExternalTransactionDTO.builder()
                            .accountNumber(externalTransaction.getSenderAccount())
                            .amount(netAmount)
                            .masterAccountNumber(masterAccount)
                            .fee(externalTransaction.getFee())
                            .note(externalTransaction.getNote())
                            .description(externalTransaction.getDescription())
                            .build());
            coreTrans = rsCoreTransaction.getBody();
            externalTransaction.setCoreRollbackCode(coreTrans != null ? coreTrans.getReferenceCode() : null);
            if (coreTrans != null)
                log.info("Core transaction id: {}", coreTrans.getId());
            count++;

            // Update receiver account balance (Napas)
            ResponseEntity<TransactionCoreNapasDTO> rsNapasTransaction = transactionNapasFeignClient.createTransaction
                    (CreateNapasTransactionDTO.builder()
                            .napasCode(externalTransaction.getNapasCode())
                            .senderAccount(externalTransaction.getSenderAccount())
                            .receiverAccount(externalTransaction.getReceiverAccount())
                            .amount(netNapasAmount)
                            .note(externalTransaction.getNote())
                            .description(externalTransaction.getDescription())
                            .build());
            napasTrans = rsNapasTransaction.getBody();
            externalTransaction.setNapasRollbackCode(napasTrans != null ? napasTrans.getId() : null);
            if (napasTrans != null)
                log.info("Napas transaction id: {}", napasTrans.getId());
            count++;

            isSuccess = rsCoreTransaction.getStatusCode().equals(HttpStatus.CREATED)
                    && rsNapasTransaction.getStatusCode().equals(HttpStatus.CREATED);

        } catch (Exception ignore) {

            if (count > 0) {

                if (coreTrans != null) {

                    // Rollback core banking transaction
                    log.warn("Rollback core transaction with code: {}", coreTrans.getReferenceCode());
                    transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                            .customerAccountNumber(externalTransaction.getSenderAccount())
                            .customerAmount(netAmount)
                            .masterAccountNumber(masterAccount)
                            .masterAmount(externalTransaction.getFee())
                            .type(TransactionType.EXTERNAL)
                            .referenceCode(coreTrans.getReferenceCode())
                            .build());
                }

                if (count > 1) {

                    if (napasTrans != null) {

                        // Rollback napas transaction
                        log.warn("Rollback napas transaction with code: {}", napasTrans.getId());
                        transactionNapasFeignClient.rollbackTransaction(NapasTransactionRollbackDTO.builder()
                                .senderAccount(externalTransaction.getSenderAccount())
                                .receiverAccount(externalTransaction.getReceiverAccount())
                                .amount(netNapasAmount)
                                .transactionId(napasTrans.getId())
                                .build());
                    }
                }
            }

            return null;
        } finally {

            // Update transaction state
            transactionStateRepository.save(TransactionState.builder()
                    .id(new ULID().nextULID())
                    .transaction(externalTransaction)
                    .state(isSuccess ? State.COMPLETED : State.FAILED)
                    .description(isSuccess ? State.COMPLETED.getDescription() : State.FAILED.getDescription())
                    .status(true)
                    .build());
        }

        // Update Transaction
        externalTransaction.setOtpCode(otp);
        ExternalTransaction transaction = transactionRepository.save(externalTransaction);

        // Update account balance
//        accountService.updateBalance(externalTransaction.getSenderAccount(), netAmount);

        try {

            // Send event to notification service
            TransactionNotificationDTO notificationDTO = transactionDetailMapper.externalEntityToNotification(transaction);
            notificationDTO.setBalance(BigDecimal.ZERO);
            notificationDTO.setSuccess(isSuccess);
            notificationService.sendTransactionNotification(notificationDTO);
        } catch (Exception e) {

            log.info(e.getMessage());
        }

        log.info("Confirm external transaction successful");
        return transactionDetailMapper.externalEntityToExtraDTO(transaction);
    }

    @Override
    public TransactionInitDTO createInternal(CreateInternalDTO create) {

        // Check account number
        log.info("Entering createInternal with parameters: create = {}", create.toString());
        if (new HashSet<>(List.of(create.getSenderAccount(), create.getReceiverAccount())).size() != 2) {

            log.error("Account number is duplicate in list: {}",
                    List.of(create.getSenderAccount(), create.getReceiverAccount()));
            throw new InvalidParameterException(
                    messageSource.getMessage(Constant.DUPLICATE_ACCOUNT_NUMBER,
                            null, LocaleContextHolder.getLocale()));
        }

        // Check customer validation (Customer service)
        try {
            CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(create.getCifCode());

            if (customer == null || !customer.isActive())
                throw new Exception();

            log.info("Customer information with CIF code({}): {}", create.getCifCode(), customer);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid customer");
            throw new InvalidParameterException(messageSource.getMessage
                    (Constant.INVALID_CUSTOMER, null, LocaleContextHolder.getLocale()));
        }

        if (!List.of(AccountType.PAYMENT, AccountType.CREDIT).contains(create.getSenderAccountType()) ||
                !List.of(AccountType.PAYMENT, AccountType.CREDIT).contains(create.getReceiverAccountType())) {

            log.error("Invalid account type");
            throw new InvalidParameterException(messageSource.getMessage
                    (Constant.INVALID_ACCOUNT_TYPE, null, LocaleContextHolder.getLocale()));
        }

        // Check sender account validation (Account service)
        AccountInfoDTO senderAccount;
        try {

            senderAccount = accountDubboService.getAccountDetail(create.getSenderAccount());

            if (senderAccount == null || !senderAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Sender account information with account number({}): {}", create.getSenderAccount(), senderAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid sender account");
            throw new InvalidParameterException(
                    messageSource.getMessage(
                            Constant.INVALID_SENDER_ACCOUNT, null, LocaleContextHolder.getLocale()));
        }

        // Check receiver account validation (Account service)
        AccountInfoDTO receiverAccount;
        try {

            receiverAccount = accountDubboService.getAccountDetail(create.getSenderAccount());

            if (receiverAccount == null || !receiverAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Receiver account information with account number({}): {}", create.getSenderAccount(), senderAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid receiver account");
            throw new InvalidParameterException(
                    messageSource.getMessage(
                            Constant.INVALID_RECEIVER_ACCOUNT, null, LocaleContextHolder.getLocale()));
        }

        // Check master account (Core banking)
        boolean isPayFee = create.getFee().compareTo(BigDecimal.ZERO) > 0;
        AccountExtraCoreDTO masterAccount = AccountExtraCoreDTO.builder().build();
        if (isPayFee) {

            ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
                    (vaultConfig.getAccountNumber());
            masterAccount = rsMasterAccount.getBody();
            if (masterAccount == null || !masterAccount.getIsActive()) {

                log.error("Invalid master account");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_MASTER_ACCOUNT, null, LocaleContextHolder.getLocale()));
            }
            log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);
        }

        try {

            // Create internal transaction
            InternalTransaction transaction = transactionDetailMapper.createToInternalEntity(create);

            // Create sender amount
            log.info("Sender (Account number: {})", create.getSenderAccount());
            BigDecimal senderAmount = create.getAmount().negate();
            log.info("Sender amount: {}", senderAmount);
            BigDecimal senderFee = create.getFeePayer().equals(FeePayer.SENDER) ? create.getFee() : BigDecimal.ZERO;
            log.info("Sender fee: {}", senderFee);
            BigDecimal senderNetAmount = senderAmount.add(senderFee.negate());
            log.info("Sender net amount: {}", senderNetAmount);

            // Create receiver amount
            log.info("Receiver (Account number: {})", create.getReceiverAccount());
            BigDecimal receiverAmount = create.getAmount();
            log.info("Receiver amount: {}", receiverAmount);
            BigDecimal receiverFee = create.getFeePayer().equals(FeePayer.RECEIVER) ? create.getFee() : BigDecimal.ZERO;
            log.info("Receiver fee: {}", receiverFee);
            BigDecimal receiverNetAmount = receiverAmount.add(receiverFee.negate());
            log.info("Receiver net amount: {}", receiverNetAmount);

            if (senderAccount.getCurrentAccountBalance().compareTo(senderNetAmount.abs()) < 0) {

                log.error("Insufficient account balance to complete transaction");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INSUFFICIENT_BALANCE, null, LocaleContextHolder.getLocale()));
            }

            // Create transaction detail
            List<TransactionDetail> detailList = new ArrayList<>(List.of(
                    TransactionDetail.builder()
                            .id(new ULID().nextULID())
                            .transaction(transaction)
                            .customerId(senderAccount.getCustomerId())
                            .account(create.getSenderAccount())
                            .amount(senderAmount)
                            .fee(senderFee)
                            .netAmount(senderNetAmount)
                            .previousBalance(senderAccount.getCurrentAccountBalance())
                            .currentBalance(senderAccount.getCurrentAccountBalance().add(senderNetAmount))
                            .availableBalance(senderAccount.getCurrentAccountBalance().add(senderNetAmount))
                            .direction(Direction.SEND)
                            .description(create.getDescription())
                            .status(true)
                            .build(),
                    TransactionDetail.builder()
                            .id(new ULID().nextULID())
                            .transaction(transaction)
                            .customerId(receiverAccount.getCustomerId())
                            .account(create.getReceiverAccount())
                            .amount(receiverAmount)
                            .fee(receiverFee)
                            .netAmount(receiverNetAmount)
                            .previousBalance(receiverAccount.getCurrentAccountBalance())
                            .currentBalance(receiverAccount.getCurrentAccountBalance().add(receiverNetAmount))
                            .availableBalance(receiverAccount.getCurrentAccountBalance().add(receiverNetAmount))
                            .direction(Direction.RECEIVE)
                            .description(create.getDescription())
                            .status(true)
                            .build()));

            if (isPayFee) {

                detailList.add(TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(masterAccount.getCustomerId())
                        .account(vaultConfig.getAccountNumber())
                        .amount(create.getFee())
                        .fee(BigDecimal.ZERO)
                        .netAmount(create.getFee())
                        .previousBalance(masterAccount.getBalance())
                        .currentBalance(masterAccount.getBalance().add(create.getFee()))
                        .availableBalance(masterAccount.getAvailableBalance().add(create.getFee()))
                        .direction(Direction.RECEIVE)
                        .description(create.getDescription())
                        .status(true)
                        .build());
            }

            // Set transaction detail
            transaction.setTransactionDetailList(detailList);

            // Set transaction state
            List<TransactionState> stateList = List.of(
                    TransactionState.builder()
                            .id(new ULID().nextULID())
                            .transaction(transaction)
                            .state(State.PENDING)
                            .description(State.PENDING.getDescription())
                            .status(true)
                            .build());
            transaction.setTransactionStateList(stateList);

            // Save transaction
            log.info("Transaction id: {}", transaction.getId());
            String otp = OTPGenerator.generateOTP(6);
            transaction.setOtpCode(otp);
            transaction.setDescription(create.getDescription());
            InternalTransaction internalTransaction = transactionRepository.save(transaction);
            String key = Constant.CACHE_TRANSACTION_PREFIX + "otp:" + internalTransaction.getId();
            redisTemplate.opsForValue().set(key, internalTransaction.getOtpCode(), Duration.ofSeconds(300));
            // Send OTP code to email (Notification service)
            notificationDubboService.sendOtpCodeTransaction(
                    OTP.builder()
                            .otp(otp)
                            .expiredTime(LocalDateTime.now().plusSeconds(300))
                            .build()
                    , create.getCifCode());
            log.info("Init internal transaction successful");
            return transactionDetailMapper.internalEntityToInit(internalTransaction);
        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.CREATE_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public TransactionExtraDTO confirmInternal(String transactionId, String otp) {

        log.info("Entering confirmInternal with parameters: transactionId = {}, otp = {}", transactionId, otp);
        InternalTransaction internalTransaction;
        try {

            // Check transaction ID
            internalTransaction = internalTransactionRepository.findById(transactionId).orElse(null);
            if (internalTransaction == null) {

                log.error("Invalid transaction id");
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.INVALID_TRANSACTION_ID, null, LocaleContextHolder.getLocale()));
            } else if (internalTransaction.getTransactionStateList().size() > 1) {

                log.error("Invalid transaction (invalid state)");
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.INVALID_TRANSACTION, null, LocaleContextHolder.getLocale()));
            }

            // Check OTP
            String key = Constant.CACHE_TRANSACTION_PREFIX + "otp:" + transactionId;
            Object otpFromRedis = redisTemplate.opsForValue().get(key);

            if (otpFromRedis == null || !otpFromRedis.toString().equals(otp)) {

                log.error("Invalid OTP code");
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.INVALID_OTP_CODE, null, LocaleContextHolder.getLocale()));
            }

        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.CREATE_FAIL, null, LocaleContextHolder.getLocale()));
        }

        // Set up data
        boolean isSuccess = false;
        String masterAccount = vaultConfig.getAccountNumber();

        // Core banking service
        TransactionCoreNapasDTO coreTrans = new TransactionCoreNapasDTO();
        BigDecimal amount = internalTransaction.getAmount();

        log.info("Sender (Account number: {})", internalTransaction.getSenderAccount());
        log.info("Sender amount: {}", amount.negate());
        BigDecimal senderFee = internalTransaction.getFeePayer().equals(FeePayer.SENDER) ?
                internalTransaction.getFee() : BigDecimal.ZERO;
        log.info("Sender fee: {}", senderFee);
        BigDecimal senderNetAmount = amount.negate().add(senderFee.negate());
        log.info("Sender net amount: {}", senderNetAmount);

        log.info("Receiver (Account number: {})", internalTransaction.getReceiverAccount());
        log.info("Receiver amount: {}", amount);
        BigDecimal receiverFee = internalTransaction.getFeePayer().equals(FeePayer.RECEIVER) ?
                internalTransaction.getFee() : BigDecimal.ZERO;
        log.info("Receiver fee: {}", receiverFee);
        BigDecimal receiverNetAmount = amount.add(receiverFee.negate());
        log.info("Receiver net amount: {}", receiverNetAmount);

        try {

            // Update sender/receiver/master account balance (Core banking)
            ResponseEntity<TransactionCoreNapasDTO> rsCoreTransaction = transactionCoreFeignClient.createInternal
                    (CreateInternalTransactionDTO.builder()
                            .senderAccountNumber(internalTransaction.getSenderAccount())
                            .senderAmount(senderNetAmount)
                            .receiverAccountNumber(internalTransaction.getReceiverAccount())
                            .receiverAmount(receiverNetAmount)
                            .masterAccountNumber(masterAccount)
                            .fee(internalTransaction.getFee())
                            .note(internalTransaction.getNote())
                            .description(internalTransaction.getDescription())
                            .build());
            coreTrans = rsCoreTransaction.getBody();
            internalTransaction.setCoreRollbackCode(coreTrans != null ? coreTrans.getReferenceCode() : null);
            if (coreTrans != null)
                log.info("Core transaction id: {}", coreTrans.getId());

            isSuccess = rsCoreTransaction.getStatusCode().equals(HttpStatus.CREATED);
        } catch (Exception ignore) {

            if (coreTrans != null) {

                // Rollback core banking transaction
                log.warn("Rollback core transaction with code: {}", coreTrans.getReferenceCode());
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .senderAccountNumber(internalTransaction.getSenderAccount())
                        .senderAmount(senderNetAmount)
                        .receiverAccountNumber(internalTransaction.getReceiverAccount())
                        .receiverAmount(receiverNetAmount)
                        .masterAccountNumber(masterAccount)
                        .masterAmount(internalTransaction.getFee())
                        .type(TransactionType.INTERNAL)
                        .referenceCode(coreTrans.getReferenceCode())
                        .build());
            }

            return null;
        } finally {

            // Update transaction state
            transactionStateRepository.save(TransactionState.builder()
                    .id(new ULID().nextULID())
                    .transaction(internalTransaction)
                    .state(isSuccess ? State.COMPLETED : State.FAILED)
                    .description(isSuccess ? State.COMPLETED.getDescription() : State.FAILED.getDescription())
                    .status(true)
                    .build());
        }

        // Update Transaction
        internalTransaction.setOtpCode(otp);
        InternalTransaction transaction = transactionRepository.save(internalTransaction);

        // Update account balance
        accountDubboService.updateBalance(internalTransaction.getSenderAccount(), senderNetAmount);
        accountDubboService.updateBalance(internalTransaction.getReceiverAccount(), receiverNetAmount);

        try {

            // Send event to notification service
            TransactionNotificationDTO notificationDTO = transactionDetailMapper.internalEntityToNotification(transaction);
            notificationDTO.setBalance(BigDecimal.ZERO);
            notificationDTO.setSuccess(isSuccess);
            notificationService.sendTransactionNotification(notificationDTO);
        } catch (Exception e) {

            log.info(e.getMessage());
        }

        log.info("Confirm internal transaction successful");
        return transactionDetailMapper.internalEntityToExtraDTO(transaction);
    }

    @Override
    public TransactionInitDTO createPayment(CreatePaymentDTO create) {

        // Check account number
        log.info("Entering createPayment with parameters: create = {}", create.toString());
        if (new HashSet<>(List.of(create.getSenderAccount(), create.getReceiverAccount())).size() != 2) {

            log.error("Account number is duplicate in list: {}",
                    List.of(create.getSenderAccount(), create.getReceiverAccount()));
            throw new InvalidParameterException(
                    messageSource.getMessage(Constant.DUPLICATE_ACCOUNT_NUMBER,
                            null, LocaleContextHolder.getLocale()));
        }

        // Check customer validation (Customer service)
        try {
            CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(create.getCifCode());

            if (customer == null || !customer.isActive())
                throw new Exception();

            log.info("Customer information with CIF code({}): {}", create.getCifCode(), customer);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid customer");
            throw new InvalidParameterException(messageSource.getMessage
                    (Constant.INVALID_CUSTOMER, null, LocaleContextHolder.getLocale()));
        }

        if (!List.of(AccountType.PAYMENT, AccountType.CREDIT).contains(create.getSenderAccountType()) ||
                !List.of(AccountType.PAYMENT, AccountType.CREDIT).contains(create.getReceiverAccountType())) {

            log.error("Invalid account type");
            throw new InvalidParameterException(messageSource.getMessage
                    (Constant.INVALID_ACCOUNT_TYPE, null, LocaleContextHolder.getLocale()));
        }

        // Check sender account validation (Account service)
        AccountInfoDTO senderAccount;
        try {

            senderAccount = accountDubboService.getAccountDetail(create.getSenderAccount());

            if (senderAccount == null || !senderAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Sender account information with account number({}): {}", create.getSenderAccount(), senderAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid sender account");
            throw new InvalidParameterException(
                    messageSource.getMessage(
                            Constant.INVALID_SENDER_ACCOUNT, null, LocaleContextHolder.getLocale()));
        }

        // Check receiver account validation (Account service)
        AccountInfoDTO receiverAccount;
        try {

            receiverAccount = accountDubboService.getAccountDetail(create.getSenderAccount());

            if (receiverAccount == null || !receiverAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Receiver account information with account number({}): {}", create.getSenderAccount(), senderAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid receiver account");
            throw new InvalidParameterException(
                    messageSource.getMessage(
                            Constant.INVALID_RECEIVER_ACCOUNT, null, LocaleContextHolder.getLocale()));
        }

        // Check master account (Core banking)
        boolean isPayFee = create.getFee().compareTo(BigDecimal.ZERO) > 0;
        AccountExtraCoreDTO masterAccount = AccountExtraCoreDTO.builder().build();
        if (isPayFee) {

            ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
                    (vaultConfig.getAccountNumber());
            masterAccount = rsMasterAccount.getBody();
            if (masterAccount == null || !masterAccount.getIsActive()) {

                log.error("Invalid master account");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_MASTER_ACCOUNT, null, LocaleContextHolder.getLocale()));
            }
            log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);
        }

        try {

            // Create payment transaction
            PaymentTransaction transaction = transactionDetailMapper.createToPaymentEntity(create);

            // Create sender (customer) amount
            log.info("Sender (Account number: {})", create.getSenderAccount());
            BigDecimal senderAmount = create.getAmount().negate();
            log.info("Sender amount: {}", senderAmount);
            BigDecimal senderFee = create.getFeePayer().equals(FeePayer.SENDER) ? create.getFee() : BigDecimal.ZERO;
            log.info("Sender fee: {}", senderFee);
            BigDecimal senderNetAmount = senderAmount.add(senderFee.negate());
            log.info("Sender net amount: {}", senderNetAmount);

            // Create receiver (provider) amount
            log.info("Receiver (Account number: {})", create.getReceiverAccount());
            BigDecimal receiverAmount = create.getAmount();
            log.info("Receiver amount: {}", receiverAmount);
            BigDecimal receiverFee = create.getFeePayer().equals(FeePayer.RECEIVER) ? create.getFee() : BigDecimal.ZERO;
            log.info("Receiver fee: {}", receiverFee);
            BigDecimal receiverNetAmount = receiverAmount.add(receiverFee.negate());
            log.info("Receiver net amount: {}", receiverNetAmount);

            if (senderAccount.getCurrentAccountBalance().compareTo(senderNetAmount.abs()) < 0) {

                log.error("Insufficient account balance to complete transaction");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INSUFFICIENT_BALANCE, null, LocaleContextHolder.getLocale()));
            }

            // Create transaction detail
            List<TransactionDetail> detailList = new ArrayList<>(List.of(
                    TransactionDetail.builder()
                            .id(new ULID().nextULID())
                            .transaction(transaction)
                            .customerId(senderAccount.getCustomerId())
                            .account(create.getSenderAccount())
                            .amount(senderAmount)
                            .fee(senderFee)
                            .netAmount(senderNetAmount)
                            .previousBalance(senderAccount.getCurrentAccountBalance())
                            .currentBalance(senderAccount.getCurrentAccountBalance().add(senderNetAmount))
                            .availableBalance(senderAccount.getCurrentAccountBalance().add(senderNetAmount))
                            .direction(Direction.SEND)
                            .description(create.getDescription())
                            .status(true)
                            .build(),
                    TransactionDetail.builder()
                            .id(new ULID().nextULID())
                            .transaction(transaction)
                            .customerId(receiverAccount.getCustomerId())
                            .account(create.getReceiverAccount())
                            .amount(receiverAmount)
                            .fee(receiverFee)
                            .netAmount(receiverNetAmount)
                            .previousBalance(receiverAccount.getCurrentAccountBalance())
                            .currentBalance(receiverAccount.getCurrentAccountBalance().add(receiverNetAmount))
                            .availableBalance(receiverAccount.getCurrentAccountBalance().add(receiverNetAmount))
                            .direction(Direction.RECEIVE)
                            .description(create.getDescription())
                            .status(true)
                            .build()));

            if (isPayFee) {

                detailList.add(TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(transaction)
                        .customerId(masterAccount.getCustomerId())
                        .account(vaultConfig.getAccountNumber())
                        .amount(create.getFee())
                        .fee(BigDecimal.ZERO)
                        .netAmount(create.getFee())
                        .previousBalance(masterAccount.getBalance())
                        .currentBalance(masterAccount.getBalance().add(create.getFee()))
                        .availableBalance(masterAccount.getAvailableBalance().add(create.getFee()))
                        .direction(Direction.RECEIVE)
                        .description(create.getDescription())
                        .status(true)
                        .build());
            }

            // Set transaction detail
            transaction.setTransactionDetailList(detailList);

            // Set transaction state
            List<TransactionState> stateList = List.of(
                    TransactionState.builder()
                            .id(new ULID().nextULID())
                            .transaction(transaction)
                            .state(State.PENDING)
                            .description(State.PENDING.getDescription())
                            .status(true)
                            .build());
            transaction.setTransactionStateList(stateList);

            // Save transaction
            log.info("Transaction id: {}", transaction.getId());
            PaymentTransaction paymentTransaction = transactionRepository.save(transaction);

            // Send OTP code to email (Notification service)
            notificationService.sendOtpCode(OTPRequestDTO.builder()
                    .email(create.getEmail())
                    .transactionId(transaction.getId())
                    .build());

            log.info("Init payment transaction successful");
            return transactionDetailMapper.paymentEntityToInit(paymentTransaction);
        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.CREATE_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public TransactionExtraDTO confirmPayment(String transactionId, String otp) {

        log.info("Entering confirmPayment with parameters: transactionId = {}, otp = {}", transactionId, otp);
        PaymentTransaction paymentTransaction;
        try {

            // Check transaction ID
            paymentTransaction = paymentTransactionRepository.findById(transactionId).orElse(null);
            if (paymentTransaction == null) {

                log.error("Invalid transaction id");
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.INVALID_TRANSACTION_ID, null, LocaleContextHolder.getLocale()));
            } else if (paymentTransaction.getTransactionStateList().size() > 1) {

                log.error("Invalid transaction (invalid state)");
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.INVALID_TRANSACTION, null, LocaleContextHolder.getLocale()));
            }

            // Check OTP
            String key = Constant.CACHE_TRANSACTION_PREFIX + "otp:" + transactionId;
            Object otpFromRedis = redisTemplate.opsForValue().get(key);

            if (otpFromRedis == null || !otpFromRedis.toString().equals(otp)) {

                log.error("Invalid OTP code");
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.INVALID_OTP_CODE, null, LocaleContextHolder.getLocale()));
            }

        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.CREATE_FAIL, null, LocaleContextHolder.getLocale()));
        }

        // Set up data
        boolean isSuccess = false;
        String masterAccount = vaultConfig.getAccountNumber();

        // Core banking service
        TransactionCoreNapasDTO coreTrans = new TransactionCoreNapasDTO();
        BigDecimal amount = paymentTransaction.getAmount();

        log.info("Sender (Account number: {})", paymentTransaction.getSenderAccount());
        log.info("Sender amount: {}", amount.negate());
        BigDecimal senderFee = paymentTransaction.getFeePayer().equals(FeePayer.SENDER) ?
                paymentTransaction.getFee() : BigDecimal.ZERO;
        log.info("Sender fee: {}", senderFee);
        BigDecimal senderNetAmount = amount.negate().add(senderFee.negate());
        ;
        log.info("Sender net amount: {}", senderNetAmount);

        log.info("Receiver (Account number: {})", paymentTransaction.getReceiverAccount());
        log.info("Receiver amount: {}", amount);
        BigDecimal receiverFee = paymentTransaction.getFeePayer().equals(FeePayer.RECEIVER) ?
                paymentTransaction.getFee() : BigDecimal.ZERO;
        log.info("Receiver fee: {}", receiverFee);
        BigDecimal receiverNetAmount = amount.add(receiverFee.negate());
        log.info("Receiver net amount: {}", receiverNetAmount);

        try {

            // Update sender/receiver/master account balance (Core banking)
            ResponseEntity<TransactionCoreNapasDTO> rsCoreTransaction = transactionCoreFeignClient.createInternal
                    (CreateInternalTransactionDTO.builder()
                            .senderAccountNumber(paymentTransaction.getSenderAccount())
                            .senderAmount(senderNetAmount)
                            .receiverAccountNumber(paymentTransaction.getReceiverAccount())
                            .receiverAmount(receiverNetAmount)
                            .masterAccountNumber(masterAccount)
                            .fee(paymentTransaction.getFee())
                            .note(paymentTransaction.getNote())
                            .description(paymentTransaction.getDescription())
                            .build());
            coreTrans = rsCoreTransaction.getBody();
            paymentTransaction.setCoreRollbackCode(coreTrans != null ? coreTrans.getReferenceCode() : null);
            if (coreTrans != null)
                log.info("Core transaction id: {}", coreTrans.getId());

            isSuccess = rsCoreTransaction.getStatusCode().equals(HttpStatus.CREATED);
        } catch (Exception ignore) {

            if (coreTrans != null) {

                // Rollback core banking transaction
                log.warn("Rollback core transaction with code: {}", coreTrans.getReferenceCode());
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .senderAccountNumber(paymentTransaction.getSenderAccount())
                        .senderAmount(senderNetAmount)
                        .receiverAccountNumber(paymentTransaction.getReceiverAccount())
                        .receiverAmount(receiverNetAmount)
                        .masterAccountNumber(masterAccount)
                        .masterAmount(paymentTransaction.getFee())
                        .type(TransactionType.INTERNAL)
                        .referenceCode(coreTrans.getReferenceCode())
                        .build());
            }

            return null;
        } finally {

            // Update transaction state
            transactionStateRepository.save(TransactionState.builder()
                    .id(new ULID().nextULID())
                    .transaction(paymentTransaction)
                    .state(isSuccess ? State.COMPLETED : State.FAILED)
                    .description(isSuccess ? State.COMPLETED.getDescription() : State.FAILED.getDescription())
                    .status(true)
                    .build());
        }

        // Update Transaction
        paymentTransaction.setOtpCode(otp);
        PaymentTransaction transaction = transactionRepository.save(paymentTransaction);

        // Update account balance
//        accountService.updateBalance(internalTransaction.getSenderAccount(), senderNetAmount);
//        accountService.updateBalance(internalTransaction.getReceiverAccount(), receiverNetAmount);

        try {

            // Send event to notification service
            TransactionNotificationDTO notificationDTO = transactionDetailMapper.paymentEntityToNotification(transaction);
            notificationDTO.setBalance(BigDecimal.ZERO);
            notificationDTO.setSuccess(isSuccess);
            notificationService.sendTransactionNotification(notificationDTO);
        } catch (Exception e) {

            log.info(e.getMessage());
        }

        log.info("Confirm payment transaction successful");
        return transactionDetailMapper.paymentEntityToExtraDTO(transaction);
    }

    @Override
    public TransactionExtraDTO createSystem(CreateSystemDTO create) {

        boolean isSender = switch (create.getAmount().compareTo(BigDecimal.ZERO)) {
            case -1:
                yield true;
            case 1:
                yield false;
            default:
                log.error("Invalid amount (max=1.000.000.000)");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_AMOUNT, null, LocaleContextHolder.getLocale()));
        };

        // Check customer account
        if (create.getCustomerAccount().equals(vaultConfig.getAccountNumber())) {

            log.error("Account number is already in use");
            throw new InvalidParameterException(
                    messageSource.getMessage(
                            Constant.DUPLICATE_ACCOUNT_NUMBER, null, LocaleContextHolder.getLocale()));
        }

        // Check customer validation (Customer service)
        try {
            CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(create.getCifCode());

            if (customer == null || !customer.isActive())
                throw new Exception();

            log.info("Customer information with CIF code({}): {}", create.getCifCode(), customer);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid customer");
            throw new InvalidParameterException(messageSource.getMessage
                    (Constant.INVALID_CUSTOMER, null, LocaleContextHolder.getLocale()));
        }

        // Check customer account validation (Account service)
//        accountService.getAccountDetail(create.getCustomerAccount());

        // Check customer account validation (Account service)
        AccountInfoDTO customerAccount;
        try {

            customerAccount = accountDubboService.getAccountDetail(create.getCustomerAccount());

            if (customerAccount == null || !customerAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Customer account information with account number({}): {}", create.getCustomerAccount(), customerAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid customer account");
            throw new InvalidParameterException(
                    messageSource.getMessage(
                            Constant.INVALID_CUSTOMER_ACCOUNT, null, LocaleContextHolder.getLocale()));
        }

        // Check master account (Core banking)
        ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
                (vaultConfig.getAccountNumber());
        AccountExtraCoreDTO masterAccount = rsMasterAccount.getBody();
        if (masterAccount == null || !masterAccount.getIsActive()) {

            log.error("Invalid master account");
            throw new InvalidParameterException(
                    messageSource.getMessage(
                            Constant.INVALID_MASTER_ACCOUNT, null, LocaleContextHolder.getLocale()));
        }
        log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);

        // Core banking service
        TransactionCoreNapasDTO coreTrans = null;

        // Create customer amount
        log.info("Customer (Account number: {})", create.getCustomerAccount());
        BigDecimal customerNetAmount = create.getAmount();
        log.info("Customer net amount: {}", customerNetAmount);

        // Create master amount
        log.info("Master (Account number: {})", vaultConfig.getAccountNumber());
        BigDecimal masterNetAmount = create.getAmount().negate();
        log.info("Master net amount: {}", masterNetAmount);

        try {

            // Create internal transaction
            InternalTransaction transaction = transactionDetailMapper.createToInternalEntity(create);
            /// Set sender account
            transaction.setSenderAccountId(isSender ? create.getCustomerAccountId() : masterAccount.getAccountId());
            transaction.setSenderAccount(isSender ? create.getCustomerAccount() : masterAccount.getAccountNumber());
            transaction.setSenderAccountType(isSender ? create.getCustomerAccountType() : masterAccount.getAccountType());
            transaction.setSenderAccountName(isSender ? create.getCustomerAccountName() : masterAccount.getCustomerName());
            /// Set receiver account
            transaction.setReceiverAccountId(!isSender ? create.getCustomerAccountId() : masterAccount.getAccountId());
            transaction.setReceiverAccount(!isSender ? create.getCustomerAccount() : masterAccount.getAccountNumber());
            transaction.setReceiverAccountType(!isSender ? create.getCustomerAccountType() : masterAccount.getAccountType());
            transaction.setReceiverAccountName(!isSender ? create.getCustomerAccountName() : masterAccount.getCustomerName());
            /// Set fee payer
            transaction.setFee(BigDecimal.ZERO);
            transaction.setFeePayer(FeePayer.SENDER);

            if (isSender) {

                if (customerAccount.getCurrentAccountBalance().compareTo(customerNetAmount.abs()) < 0) {

                    log.error("Insufficient account balance to complete transaction");
                    throw new InvalidParameterException(
                            messageSource.getMessage(
                                    Constant.INSUFFICIENT_BALANCE, null, LocaleContextHolder.getLocale()));
                }
            } else if (masterAccount.getAvailableBalance().compareTo(masterNetAmount.abs()) < 0) {

                log.error("Insufficient master account balance to complete transaction");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INSUFFICIENT_MASTER_BALANCE, null, LocaleContextHolder.getLocale()));
            }

            // Create transaction detail
            List<TransactionDetail> detailList = new ArrayList<>(List.of(
                    TransactionDetail.builder()
                            .id(new ULID().nextULID())
                            .transaction(transaction)
                            .customerId(customerAccount.getCustomerId())
                            .account(create.getCustomerAccount())
                            .amount(customerNetAmount)
                            .fee(BigDecimal.ZERO)
                            .netAmount(customerNetAmount)
                            .previousBalance(customerAccount.getCurrentAccountBalance())
                            .currentBalance(customerAccount.getCurrentAccountBalance().add(customerNetAmount))
                            .availableBalance(customerAccount.getCurrentAccountBalance().add(customerNetAmount))
                            .direction(isSender ? Direction.SEND : Direction.RECEIVE)
                            .description(create.getDescription())
                            .status(true)
                            .build(),
                    TransactionDetail.builder()
                            .id(new ULID().nextULID())
                            .transaction(transaction)
                            .customerId(masterAccount.getCustomerId())
                            .account(vaultConfig.getAccountNumber())
                            .amount(masterNetAmount)
                            .fee(BigDecimal.ZERO)
                            .netAmount(masterNetAmount)
                            .previousBalance(masterAccount.getBalance())
                            .currentBalance(masterAccount.getBalance().add(masterNetAmount))
                            .availableBalance(masterAccount.getAvailableBalance().add(masterNetAmount))
                            .direction(!isSender ? Direction.SEND : Direction.RECEIVE)
                            .description(create.getDescription())
                            .status(true)
                            .build()));

            // Set transaction detail
            transaction.setTransactionDetailList(detailList);

            // Set transaction state
            List<TransactionState> stateList = List.of(
                    TransactionState.builder()
                            .id(new ULID().nextULID())
                            .transaction(transaction)
                            .state(State.COMPLETED)
                            .description(State.COMPLETED.getDescription())
                            .status(true)
                            .build());
            transaction.setTransactionStateList(stateList);

            try {

                // Update customer/master account balance (Core banking)
                ResponseEntity<TransactionCoreNapasDTO> rsCoreTransaction = transactionCoreFeignClient.createSystem
                        (CreateSystemTransactionDTO.builder()
                                .customerAccountNumber(customerAccount.getAccountNumber())
                                .customerAmount(customerNetAmount)
                                .masterAccountNumber(masterAccount.getAccountNumber())
                                .masterAmount(masterNetAmount)
                                .note(create.getNote())
                                .description(create.getDescription())
                                .build());
                coreTrans = rsCoreTransaction.getBody();
                transaction.setCoreRollbackCode(coreTrans != null ? coreTrans.getReferenceCode() : null);
                if (coreTrans != null)
                    log.info("Core transaction id: {}", coreTrans.getId());

            } catch (Exception ignore) {

                if (coreTrans != null) {

                    // Rollback core banking transaction
                    log.warn("Rollback core transaction with code: {}", coreTrans.getReferenceCode());
                    transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                            .customerAccountNumber(customerAccount.getAccountNumber())
                            .customerAmount(customerNetAmount)
                            .masterAccountNumber(masterAccount.getAccountNumber())
                            .masterAmount(masterNetAmount)
                            .type(TransactionType.SYSTEM)
                            .referenceCode(coreTrans.getReferenceCode())
                            .build());
                }

                return null;
            }

            // Save transaction
            log.info("Transaction id: {}", transaction.getId());
            InternalTransaction internalTransaction = transactionRepository.save(transaction);

            // Update account balance
//        accountService.updateBalance(create.getCustomerAccount(), customerNetAmount);

            return transactionDetailMapper.internalEntityToExtraDTO(internalTransaction);
        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            if (coreTrans != null)

                // Rollback core banking transaction
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .customerAccountNumber(customerAccount.getAccountNumber())
                        .customerAmount(customerNetAmount)
                        .masterAccountNumber(masterAccount.getAccountNumber())
                        .masterAmount(masterNetAmount)
                        .type(TransactionType.SYSTEM)
                        .referenceCode(coreTrans.getReferenceCode())
                        .build());

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.CREATE_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void rollback(String transactionId) {

        try {

            log.info("Entering rollback with parameters: transactionId = {}", transactionId);
            Optional<Transaction> transaction = transactionRepository.findById(transactionId);
            if (transaction.isPresent()) {

                log.info("Transaction is exist with transaction id: {}", transactionId);
                List<TransactionState> listState = transaction.get().getTransactionStateList().stream().toList();
                if (listState.get(listState.size() - 1).getState().equals(State.PENDING)) {

                    log.error("Invalid transaction (invalid state)");
                    throw new InvalidParameterException(
                            messageSource.getMessage(Constant.INVALID_TRANSACTION, null, LocaleContextHolder.getLocale()));
                } else {

                    if (transaction.get() instanceof ExternalTransaction externalTransaction) {

                        this.rollbackExternal(externalTransaction);
                    } else if (transaction.get() instanceof InternalTransaction internalTransaction) {

                        this.rollbackInternal(internalTransaction);
                    } else if (transaction.get() instanceof PaymentTransaction paymentTransaction) {

                        this.rollbackPayment(paymentTransaction);
                    } else {

                        log.error("Invalid transaction id");
                        throw new InvalidParameterException(
                                messageSource.getMessage(Constant.INVALID_TRANSACTION_ID, null, LocaleContextHolder.getLocale()));
                    }
                }
            } else {

                log.error("Invalid transaction id");
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.INVALID_TRANSACTION_ID, null, LocaleContextHolder.getLocale()));
            }
        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            if (e instanceof FeignException feignException)
                throw feignException;

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.ROLLBACK_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public Map<String, Object> getAllByCifCode(Integer limit, Integer page, String accountBankingNumber) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<TransactionHistoryProjection> transactionPage = transactionRepository.findByBankingAccountNumber(accountBankingNumber, pageable);
        Map<String, Object> dataResponse = new HashMap<>();
        dataResponse.put("totalRecord", transactionPage.getTotalElements());
        dataResponse.put("totalPages", transactionPage.getTotalPages());
        List<TransactionHistoryRp> transactionHistoryRpList = transactionPage
                .stream()
                .map(MapToDto::mapTransactionHistoryRp)
                .toList();
        dataResponse.put("content", transactionHistoryRpList);
        return dataResponse;
    }

    private void rollbackExternal(ExternalTransaction externalTransaction) {

        // Check sender account validation (Core banking)
        log.info("Entering rollbackExternal with id: id = {}", externalTransaction.getId());
        AccountInfoDTO senderAccount;
        try {

            senderAccount = accountDubboService.getAccountDetail(externalTransaction.getSenderAccount());

            if (senderAccount == null || !senderAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Sender account information with account number({}): {}", externalTransaction.getSenderAccount(), senderAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid sender account");
            throw new InvalidParameterException(
                    messageSource.getMessage(
                            Constant.INVALID_SENDER_ACCOUNT, null, LocaleContextHolder.getLocale()));
        }

        // Check master account (Core banking)
        boolean isPayFee = externalTransaction.getFee().compareTo(BigDecimal.ZERO) > 0;
        AccountExtraCoreDTO masterAccount = AccountExtraCoreDTO.builder().build();
        if (isPayFee) {

            ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
                    (vaultConfig.getAccountNumber());
            masterAccount = rsMasterAccount.getBody();
            if (masterAccount == null || !masterAccount.getIsActive()) {

                log.error("Invalid master account");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_MASTER_ACCOUNT, null, LocaleContextHolder.getLocale()));
            }
            log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);
        }

        // Sender amount
        log.info("Sender (Account number: {})", externalTransaction.getSenderAccount());
        BigDecimal amount = externalTransaction.getAmount().negate();
        log.info("Amount: {}", amount);
        BigDecimal fee = externalTransaction.getFeePayer().equals(FeePayer.SENDER) ?
                externalTransaction.getFee() : BigDecimal.ZERO;
        log.info("Fee: {}", fee);
        BigDecimal netAmount = amount.add(fee.negate());
        log.info("Net amount: {}", netAmount);

        // Receiver amount
        log.info("Receiver (Account number: {})", externalTransaction.getReceiverAccount());
        BigDecimal napasAmount = externalTransaction.getAmount();
        log.info("Napas amount: {}", napasAmount);
        BigDecimal napasFee = externalTransaction.getFeePayer().equals(FeePayer.RECEIVER) ?
                externalTransaction.getFee() : BigDecimal.ZERO;
        log.info("Napas fee: {}", napasFee);
        BigDecimal netNapasAmount = napasAmount.add(napasFee.negate());
        log.info("Net napas amount: {}", netNapasAmount);

        try {

            // Rollback core transaction
            if (externalTransaction.getCoreRollbackCode() != null) {

                log.warn("Rollback core transaction with code: {}", externalTransaction.getCoreRollbackCode());
                transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                        .customerAccountNumber(externalTransaction.getSenderAccount())
                        .customerAmount(netAmount)
                        .masterAccountNumber(masterAccount.getAccountNumber())
                        .masterAmount(externalTransaction.getFee())
                        .type(TransactionType.EXTERNAL)
                        .referenceCode(externalTransaction.getCoreRollbackCode())
                        .build());
            }

            // Rollback napas transaction
            if (externalTransaction.getNapasRollbackCode() != null) {

                // Rollback napas transaction
                log.warn("Rollback napas transaction with code: {}", externalTransaction.getNapasRollbackCode());
                transactionNapasFeignClient.rollbackTransaction(NapasTransactionRollbackDTO.builder()
                        .senderAccount(externalTransaction.getSenderAccount())
                        .receiverAccount(externalTransaction.getReceiverAccount())
                        .amount(netNapasAmount)
                        .transactionId(externalTransaction.getNapasRollbackCode())
                        .build());
            }

            // Create rollback transaction
            ExternalTransaction rollback = transactionDetailMapper.entityToRollback(externalTransaction);

            // Set transaction state
            rollback.setTransactionStateList(List.of(TransactionState.builder()
                    .id(new ULID().nextULID())
                    .transaction(rollback)
                    .state(State.COMPLETED)
                    .description(State.COMPLETED.getDescription())
                    .status(true)
                    .build()));

            // Create transaction detail list
            List<TransactionDetail> details = new ArrayList<>(List.of(TransactionDetail.builder()
                    .id(new ULID().nextULID())
                    .transaction(rollback)
                    .customerId(senderAccount.getCustomerId())
                    .account(senderAccount.getAccountNumber())
                    .amount(amount.negate())
                    .fee(fee)
                    .netAmount(netAmount.negate())
                    .previousBalance(senderAccount.getCurrentAccountBalance())
                    .currentBalance(senderAccount.getCurrentAccountBalance().add(netAmount.negate()))
                    .availableBalance(senderAccount.getCurrentAccountBalance().add(netAmount.negate()))
                    .direction(Direction.RECEIVE)
                    .description(ROLLBACK_MESSAGE + externalTransaction
                            .getTransactionDetailList().stream()
                            .filter(e -> e.getAccount().equals(senderAccount.getAccountNumber()))
                            .findFirst().orElse(new TransactionDetail()).getId())
                    .status(true)
                    .build()));

            if (isPayFee) {

                // Add transaction detail
                BigDecimal masterAmount = externalTransaction.getFee().negate();
                log.info("Master amount: {}", masterAmount);
                details.add(TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(masterAccount.getCustomerId())
                        .account(masterAccount.getAccountNumber())
                        .amount(masterAmount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(masterAmount)
                        .previousBalance(masterAccount.getBalance())
                        .currentBalance(masterAccount.getBalance().add(masterAmount))
                        .availableBalance(masterAccount.getAvailableBalance().add(masterAmount))
                        .direction(Direction.SEND)
                        .description(ROLLBACK_MESSAGE + externalTransaction
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(vaultConfig.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build());
            }

            // Set transaction detail list
            rollback.setTransactionDetailList(details);

            // Update account balance
//        accountService.updateBalance(externalTransaction.getSenderAccount(), netAmount.negate());

            log.info("Rollback external transaction successful");
            transactionRepository.save(rollback);
        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.ROLLBACK_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    private void rollbackInternal(InternalTransaction internalTransaction) {

        // Check sender account validation (Account service)
        log.info("Entering rollbackInternal with id: id = {}", internalTransaction.getId());
        AccountInfoDTO senderAccount;
        try {

            senderAccount = accountDubboService.getAccountDetail(internalTransaction.getSenderAccount());

            if (senderAccount == null || !senderAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Sender account information with account number({}): {}", internalTransaction.getSenderAccount(), senderAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid sender account");
            throw new InvalidParameterException(
                    messageSource.getMessage(
                            Constant.INVALID_SENDER_ACCOUNT, null, LocaleContextHolder.getLocale()));
        }

        // Check receiver account validation (Account service)
        AccountInfoDTO receiverAccount;
        try {

            receiverAccount = accountDubboService.getAccountDetail(internalTransaction.getSenderAccount());

            if (receiverAccount == null || !receiverAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Receiver account information with account number({}): {}", internalTransaction.getSenderAccount(), senderAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid receiver account");
            throw new InvalidParameterException(
                    messageSource.getMessage(
                            Constant.INVALID_RECEIVER_ACCOUNT, null, LocaleContextHolder.getLocale()));
        }

        // Check master account (Core banking)
        boolean isPayFee = internalTransaction.getFee().compareTo(BigDecimal.ZERO) > 0;
        AccountExtraCoreDTO masterAccount = AccountExtraCoreDTO.builder().build();
        if (isPayFee) {

            ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
                    (vaultConfig.getAccountNumber());
            masterAccount = rsMasterAccount.getBody();
            if (masterAccount == null || !masterAccount.getIsActive()) {

                log.error("Invalid master account");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_MASTER_ACCOUNT, null, LocaleContextHolder.getLocale()));
            }
            log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);
        }

        // Sender amount
        log.info("Sender (Account number: {})", internalTransaction.getSenderAccount());
        BigDecimal senderAmount = internalTransaction.getAmount().negate();
        log.info("Amount: {}", senderAmount);
        BigDecimal senderFee = internalTransaction.getFeePayer().equals(FeePayer.SENDER) ?
                internalTransaction.getFee() : BigDecimal.ZERO;
        log.info("Fee: {}", senderFee);
        BigDecimal senderNetAmount = senderAmount.add(senderFee.negate());
        log.info("Net amount: {}", senderNetAmount);

        // Receiver amount
        log.info("Receiver (Account number: {})", internalTransaction.getReceiverAccount());
        BigDecimal receiverAmount = internalTransaction.getAmount();
        log.info("Napas amount: {}", receiverAmount);
        BigDecimal receiverFee = internalTransaction.getFeePayer().equals(FeePayer.RECEIVER) ?
                internalTransaction.getFee() : BigDecimal.ZERO;
        log.info("Napas fee: {}", receiverFee);
        BigDecimal receiverNetAmount = receiverAmount.add(receiverFee.negate());
        log.info("Net napas amount: {}", receiverNetAmount);

        try {

            // Rollback core banking transaction
            log.warn("Rollback core transaction with code: {}", internalTransaction.getCoreRollbackCode());
            transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                    .senderAccountNumber(internalTransaction.getSenderAccount())
                    .senderAmount(senderNetAmount)
                    .receiverAccountNumber(internalTransaction.getReceiverAccount())
                    .receiverAmount(receiverNetAmount)
                    .masterAccountNumber(masterAccount.getAccountNumber())
                    .masterAmount(internalTransaction.getFee())
                    .type(TransactionType.INTERNAL)
                    .referenceCode(internalTransaction.getCoreRollbackCode())
                    .build());

            // Create rollback transaction
            InternalTransaction rollback = transactionDetailMapper.entityToRollback(internalTransaction);

            // Set transaction state
            rollback.setTransactionStateList(List.of(TransactionState.builder()
                    .id(new ULID().nextULID())
                    .transaction(rollback)
                    .state(State.COMPLETED)
                    .description(State.COMPLETED.getDescription())
                    .status(true)
                    .build()));

            // Create transaction detail list
            List<TransactionDetail> details = new ArrayList<>(List.of(TransactionDetail.builder()
                            .id(new ULID().nextULID())
                            .transaction(rollback)
                            .customerId(senderAccount.getCustomerId())
                            .account(senderAccount.getAccountNumber())
                            .amount(senderAmount.negate())
                            .fee(senderFee)
                            .netAmount(senderNetAmount.negate())
                            .previousBalance(senderAccount.getCurrentAccountBalance())
                            .currentBalance(senderAccount.getCurrentAccountBalance().add(senderNetAmount.negate()))
                            .availableBalance(senderAccount.getCurrentAccountBalance().add(senderNetAmount.negate()))
                            .direction(Direction.RECEIVE)
                            .description(ROLLBACK_MESSAGE + internalTransaction
                                    .getTransactionDetailList().stream()
                                    .filter(e -> e.getAccount().equals(senderAccount.getAccountNumber()))
                                    .findFirst().orElse(new TransactionDetail()).getId())
                            .status(true)
                            .build(),
                    TransactionDetail.builder()
                            .id(new ULID().nextULID())
                            .transaction(rollback)
                            .customerId(receiverAccount.getCustomerId())
                            .account(receiverAccount.getAccountNumber())
                            .amount(receiverAmount.negate())
                            .fee(receiverFee)
                            .netAmount(receiverNetAmount.negate())
                            .previousBalance(receiverAccount.getCurrentAccountBalance())
                            .currentBalance(receiverAccount.getCurrentAccountBalance().add(receiverNetAmount.negate()))
                            .availableBalance(receiverAccount.getCurrentAccountBalance().add(receiverNetAmount.negate()))
                            .direction(Direction.RECEIVE)
                            .description(ROLLBACK_MESSAGE + internalTransaction
                                    .getTransactionDetailList().stream()
                                    .filter(e -> e.getAccount().equals(receiverAccount.getAccountNumber()))
                                    .findFirst().orElse(new TransactionDetail()).getId())
                            .status(true)
                            .build()));

            if (isPayFee) {

                // Add transaction detail
                BigDecimal masterAmount = internalTransaction.getFee().negate();
                log.info("Master amount: {}", masterAmount);
                details.add(TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(masterAccount.getCustomerId())
                        .account(masterAccount.getAccountNumber())
                        .amount(masterAmount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(masterAmount)
                        .previousBalance(masterAccount.getBalance())
                        .currentBalance(masterAccount.getBalance().add(masterAmount))
                        .availableBalance(masterAccount.getAvailableBalance().add(masterAmount))
                        .direction(Direction.SEND)
                        .description(ROLLBACK_MESSAGE + internalTransaction
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(vaultConfig.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build());

                // Set transaction detail list
                rollback.setTransactionDetailList(details);

                // Update account balance
//        accountService.updateBalance(internalTransaction.getSenderAccount(), senderNetAmount.negate());
//        accountService.updateBalance(internalTransaction.getReceiverAccount(), receiverNetAmount.negate());

                log.info("Rollback internal transaction successful");
                transactionRepository.save(rollback);
            }

        } catch (Exception e) {

            if (!(e instanceof InvalidParameterException)) {

                log.error(e.getMessage());
            }

            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.ROLLBACK_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    private void rollbackPayment(PaymentTransaction paymentTransaction) {

        // Check sender account validation (Account service)
        log.info("Entering rollbackPayment with id: id = {}", paymentTransaction.getId());
        AccountInfoDTO senderAccount;
        try {

            senderAccount = accountDubboService.getAccountDetail(paymentTransaction.getSenderAccount());

            if (senderAccount == null || !senderAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Sender account information with account number({}): {}", paymentTransaction.getSenderAccount(), senderAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid sender account");
            throw new InvalidParameterException(
                    messageSource.getMessage(
                            Constant.INVALID_SENDER_ACCOUNT, null, LocaleContextHolder.getLocale()));
        }

        // Check receiver account validation (Account service)
        AccountInfoDTO receiverAccount;
        try {

            receiverAccount = accountDubboService.getAccountDetail(paymentTransaction.getSenderAccount());

            if (receiverAccount == null || !receiverAccount.getStatusAccount().equals(ObjectStatus.ACTIVE))
                throw new Exception();

            log.info("Receiver account information with account number({}): {}", paymentTransaction.getSenderAccount(), senderAccount);
        } catch (Exception e) {

            if (e instanceof DubboException) {

                log.error(e.getMessage());
            }

            log.error("Invalid receiver account");
            throw new InvalidParameterException(
                    messageSource.getMessage(
                            Constant.INVALID_RECEIVER_ACCOUNT, null, LocaleContextHolder.getLocale()));
        }

        // Check master account (Core banking)
        boolean isPayFee = paymentTransaction.getFee().compareTo(BigDecimal.ZERO) > 0;
        AccountExtraCoreDTO masterAccount = AccountExtraCoreDTO.builder().build();
        if (isPayFee) {

            ResponseEntity<AccountExtraCoreDTO> rsMasterAccount = accountCoreFeignClient.getByAccountNumber
                    (vaultConfig.getAccountNumber());
            masterAccount = rsMasterAccount.getBody();
            if (masterAccount == null || !masterAccount.getIsActive()) {

                log.error("Invalid master account");
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_MASTER_ACCOUNT, null, LocaleContextHolder.getLocale()));
            }
            log.info("Master account information with account({}): {}", vaultConfig.getAccountNumber(), masterAccount);
        }


        // Sender (customer) amount
        log.info("Sender (Account number: {})", paymentTransaction.getSenderAccount());
        BigDecimal senderAmount = paymentTransaction.getAmount().negate();
        log.info("Sender amount: {}", senderAmount);
        BigDecimal senderFee = paymentTransaction.getFeePayer().equals(FeePayer.SENDER) ?
                paymentTransaction.getFee() : BigDecimal.ZERO;
        log.info("Sender fee: {}", senderFee);
        BigDecimal senderNetAmount = senderAmount.add(senderFee.negate());
        log.info("Sender net amount: {}", senderNetAmount);

        // Receiver (provider) amount
        log.info("Receiver (Account number: {})", paymentTransaction.getReceiverAccount());
        BigDecimal receiverAmount = paymentTransaction.getAmount();
        log.info("Receiver amount: {}", receiverAmount);
        BigDecimal receiverFee = paymentTransaction.getFeePayer().equals(FeePayer.RECEIVER) ?
                paymentTransaction.getFee() : BigDecimal.ZERO;
        log.info("Receiver fee: {}", receiverFee);
        BigDecimal receiverNetAmount = receiverAmount.add(receiverFee.negate());
        log.info("Receiver net amount: {}", receiverNetAmount);

        try {

            // Rollback core banking transaction
            log.warn("Rollback core transaction with code: {}", paymentTransaction.getCoreRollbackCode());
            transactionCoreFeignClient.rollback(CoreTransactionRollbackDTO.builder()
                    .senderAccountNumber(paymentTransaction.getSenderAccount())
                    .senderAmount(senderNetAmount)
                    .receiverAccountNumber(paymentTransaction.getReceiverAccount())
                    .receiverAmount(receiverNetAmount)
                    .masterAccountNumber(masterAccount.getAccountNumber())
                    .masterAmount(paymentTransaction.getFee())
                    .type(TransactionType.INTERNAL)
                    .referenceCode(paymentTransaction.getCoreRollbackCode())
                    .build());

            // Create rollback transaction
            PaymentTransaction rollback = transactionDetailMapper.entityToRollback(paymentTransaction);

            // Set transaction state
            rollback.setTransactionStateList(List.of(TransactionState.builder()
                    .id(new ULID().nextULID())
                    .transaction(rollback)
                    .state(State.COMPLETED)
                    .description(State.COMPLETED.getDescription())
                    .status(true)
                    .build()));

            // Create transaction detail list
            List<TransactionDetail> details = new ArrayList<>(List.of(
                    TransactionDetail.builder()
                            .id(new ULID().nextULID())
                            .transaction(rollback)
                            .customerId(senderAccount.getCustomerId())
                            .account(senderAccount.getAccountNumber())
                            .amount(senderAmount.negate())
                            .fee(senderFee)
                            .netAmount(senderNetAmount.negate())
                            .previousBalance(senderAccount.getCurrentAccountBalance())
                            .currentBalance(senderAccount.getCurrentAccountBalance().add(senderNetAmount.negate()))
                            .availableBalance(senderAccount.getCurrentAccountBalance().add(senderNetAmount.negate()))
                            .direction(Direction.SEND)
                            .description(ROLLBACK_MESSAGE + paymentTransaction
                                    .getTransactionDetailList().stream()
                                    .filter(e -> e.getAccount().equals(senderAccount.getAccountNumber()))
                                    .findFirst().orElse(new TransactionDetail()).getId())
                            .status(true)
                            .build(),
                    TransactionDetail.builder()
                            .id(new ULID().nextULID())
                            .transaction(rollback)
                            .customerId(receiverAccount.getCustomerId())
                            .account(receiverAccount.getAccountNumber())
                            .amount(receiverAmount.negate())
                            .fee(receiverFee)
                            .netAmount(receiverNetAmount.negate())
                            .previousBalance(receiverAccount.getCurrentAccountBalance())
                            .currentBalance(receiverAccount.getCurrentAccountBalance().add(receiverNetAmount.negate()))
                            .availableBalance(receiverAccount.getCurrentAccountBalance().add(receiverNetAmount.negate()))
                            .direction(Direction.RECEIVE)
                            .description(ROLLBACK_MESSAGE + paymentTransaction
                                    .getTransactionDetailList().stream()
                                    .filter(e -> e.getAccount().equals(receiverAccount.getAccountNumber()))
                                    .findFirst().orElse(new TransactionDetail()).getId())
                            .status(true)
                            .build()));

            if (isPayFee) {

                // Add transaction detail
                BigDecimal masterAmount = paymentTransaction.getFee().negate();
                log.info("Master amount: {}", masterAmount);
                details.add(TransactionDetail.builder()
                        .id(new ULID().nextULID())
                        .transaction(rollback)
                        .customerId(masterAccount.getCustomerId())
                        .account(masterAccount.getAccountNumber())
                        .amount(masterAmount)
                        .fee(BigDecimal.ZERO)
                        .netAmount(masterAmount)
                        .previousBalance(masterAccount.getBalance())
                        .currentBalance(masterAccount.getBalance().add(masterAmount))
                        .availableBalance(masterAccount.getAvailableBalance().add(masterAmount))
                        .direction(Direction.SEND)
                        .description(ROLLBACK_MESSAGE + paymentTransaction
                                .getTransactionDetailList().stream()
                                .filter(e -> e.getAccount().equals(vaultConfig.getAccountNumber()))
                                .findFirst().orElse(new TransactionDetail()).getId())
                        .status(true)
                        .build());

                // Set transaction detail list
                rollback.setTransactionDetailList(details);

                // Update account balance
//        accountService.updateBalance(internalTransaction.getSenderAccount(), senderNetAmount.negate());
//        accountService.updateBalance(internalTransaction.getReceiverAccount(), receiverNetAmount.negate());

                log.info("Rollback payment transaction successful");
                transactionRepository.save(rollback);
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
