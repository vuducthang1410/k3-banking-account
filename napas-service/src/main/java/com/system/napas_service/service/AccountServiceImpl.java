package com.system.napas_service.service;

import com.system.common_library.dto.response.account.AccountExtraNapasDTO;
import com.system.common_library.dto.response.account.AccountNapasDTO;
import com.system.napas_service.dto.account.AccountDTO;
import com.system.napas_service.dto.account.CreateAccountDTO;
import com.system.napas_service.dto.account.UpdateAccountDTO;
import com.system.napas_service.dto.response.PagedDTO;
import com.system.napas_service.entity.Account;
import com.system.napas_service.mapper.AccountMapper;
import com.system.napas_service.repository.AccountRepository;
import com.system.napas_service.service.interfaces.AccountService;
import com.system.napas_service.service.interfaces.PagingService;
import com.system.napas_service.util.Constant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
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

    @Override
    public AccountExtraNapasDTO findByAccountNumber(String accountNumber) {

        log.info("Entering findByAccountNumber with parameters: accountNumber = {}", accountNumber);
        Optional<Account> account = accountRepository.findByAccountNumberAndStatus(accountNumber, true);

        return account.map(accountMapper::entityToExtraNapasDTO).orElse(null);
    }

    @Override
    public PagedDTO<AccountNapasDTO> findAllByCondition(Boolean isActive, String search, String sort, int page, int limit) {

        log.info("Entering findAllByCondition with parameters: " +
                        "isActive = {}, search = {}, sort = {}, page = {}, limit = {}", isActive, search, sort, page, limit);
        Pageable pageable = pagingService.getPageable(sort, page, limit, Account.class);
        Page<Account> pageResult = accountRepository.findAllByCondition(true, isActive, search, pageable);

        return new PagedDTO<>(pageResult.map(accountMapper::entityToNapasDTO));
    }

    @Override
    public AccountDTO create(CreateAccountDTO create) {

        try {

            log.info("Entering create with parameters: create = {}", create.toString());
            Account account = accountMapper.createToEntity(create);
            log.info("Created a new account number: {}", account.getAccountNumber());
            return accountMapper.entityToDTO(accountRepository.save(account));
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new InvalidParameterException(
                    messageSource.getMessage(Constant.CREATE_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public AccountDTO update(UpdateAccountDTO update, String accountNumber) {

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
