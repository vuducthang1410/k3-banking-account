package com.system.core_banking_service.service;

import com.system.common_library.dto.request.account.CreateAccountCoreDTO;
import com.system.common_library.dto.request.account.UpdateAccountCoreDTO;
import com.system.common_library.dto.response.PagedDTO;
import com.system.common_library.dto.response.account.AccountCoreDTO;
import com.system.common_library.dto.response.account.AccountExtraCoreDTO;
import com.system.common_library.enums.AccountType;
import com.system.core_banking_service.entity.Account;
import com.system.core_banking_service.entity.Customer;
import com.system.core_banking_service.mapper.AccountMapper;
import com.system.core_banking_service.repository.AccountRepository;
import com.system.core_banking_service.repository.CustomerRepository;
import com.system.core_banking_service.service.interfaces.AccountService;
import com.system.core_banking_service.service.interfaces.PagingService;
import com.system.core_banking_service.util.AccountNumberGenerator;
import com.system.core_banking_service.util.Constant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final MessageSource messageSource;

    private final AccountMapper accountMapper;

    private final PagingService pagingService;

    private final AccountRepository accountRepository;

    private final CustomerRepository customerRepository;

    @Override
    public AccountExtraCoreDTO findByAccountNumber(String accountNumber) {

        log.info("Entering findByAccountNumber with parameters: accountNumber = {}", accountNumber);
        Optional<Account> account = accountRepository.findByAccountNumberAndStatus(accountNumber, true);

        return account.map(accountMapper::entityToExtraDTO).orElse(null);
    }

    @Override
    public PagedDTO<AccountCoreDTO> findAllByCondition
            (List<AccountType> typeList, Boolean isActive, String search, String sort, int page, int limit) {

        log.info("Entering findAllByCondition with parameters: " +
                        "typeList = {}, isActive = {}, search = {}, sort = {}, page = {}, limit = {}",
                typeList, isActive, search, sort, page, limit);
        Pageable pageable = pagingService.getPageable(sort, page, limit, Account.class);
        Page<Account> pageResult = accountRepository.findAllByCondition(true, typeList, isActive, search, pageable);

        return new PagedDTO<>(pageResult.map(accountMapper::entityToDTO));
    }

    @Override
    public AccountCoreDTO create(CreateAccountCoreDTO create) {

        try {

            log.info("Entering create with parameters: create = {}", create.toString());
            if (!customerRepository.existsByCifCode(create.getCifCode())) {

                log.error("Invalid CIF code: {}", create.getCifCode());
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_CIF_CODE, null, LocaleContextHolder.getLocale()));
            }

            Optional<Customer> customer = customerRepository.findByCifCodeAndStatus(create.getCifCode(), true);
            if (customer.isEmpty()) {

                log.error("Invalid customer with CIF code: {}", create.getCifCode());
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.INVALID_CUSTOMER, null, LocaleContextHolder.getLocale()));
            }

            log.info("Customer is exist");
            Account account = accountMapper.createToEntity(create);

            // Re-generate account number if duplicate
            while (accountRepository.existsByAccountNumber(account.getAccountNumber())) {

                log.warn("Duplicate account number: {}", account.getAccountNumber());
                account.setAccountNumber(AccountNumberGenerator.generateAccountNumber
                        (create.getType(), create.getCifCode()));
            }

            account.setCustomer(customer.get());
            log.info("Created a new account number: {}", account.getAccountNumber());

            return accountMapper.entityToDTO(accountRepository.save(account));
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.CREATE_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public AccountCoreDTO update(UpdateAccountCoreDTO update, String accountNumber) {

        log.info("Entering update with parameters: accountNumber = {}, update = {}", accountNumber, update.toString());
        Optional<Account> entity = accountRepository.findByAccountNumberAndStatus(accountNumber, true);
        if (entity.isPresent()) {

            log.info("Account is exist");
            try {

                Account account = accountMapper.updateToEntity(update, entity.get());
                return accountMapper.entityToDTO(accountRepository.save(account));
            } catch (Exception e) {

                log.error(e.getMessage());
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.UPDATE_FAIL, null, LocaleContextHolder.getLocale()));
            }
        } else {

            log.info("Account is not exist");
            throw new InvalidParameterException(
                    messageSource.getMessage(Constant.INVALID_ACCOUNT, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void delete(String id) {

        log.info("Entering delete with parameters: id = {}", id);
        Optional<Account> account = accountRepository.findByIdAndStatus(id, true);
        if (account.isPresent()) {

            log.info("Account is exist");
            try {

                account.get().setStatus(false);
                log.info("Set status to false");
                accountRepository.save(account.get());
            } catch (Exception e) {

                log.error(e.getMessage());
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.DELETE_FAIL, null, LocaleContextHolder.getLocale()));
            }
        } else {

            log.info("Account is not exist");
            throw new InvalidParameterException(
                    messageSource.getMessage(Constant.INVALID_ACCOUNT, null, LocaleContextHolder.getLocale()));
        }
    }
}
