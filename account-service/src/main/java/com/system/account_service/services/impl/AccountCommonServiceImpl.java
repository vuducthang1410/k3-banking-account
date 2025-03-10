package com.system.account_service.services.impl;

import com.system.account_service.dtos.account_common.CreateAccountCommonDTO;
import com.system.account_service.dtos.response.PageDataDTO;
import com.system.account_service.entities.AccountCommons;
import com.system.account_service.entities.type.AccountStatus;
import com.system.account_service.entities.type.AccountTypes;
import com.system.account_service.exception.payload.ExistedDataException;
import com.system.account_service.exception.payload.ResourceNotFoundException;
import com.system.account_service.repositories.AccountCommonRepository;
import com.system.account_service.services.AccountCommonService;
import com.system.account_service.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountCommonServiceImpl implements AccountCommonService {
    private final AccountCommonRepository repository;

    @Override
    public AccountCommons create(CreateAccountCommonDTO data) {
        // ***
        // Kiem tra accountNumber duplicated
        // ***
        Boolean existedAccountNumber = repository.existsByAccountNumber(data.getAccountNumber());
        if(existedAccountNumber){
            throw new ExistedDataException(MessageKeys.EXISTED_ACCOUNT_NUMBER);
        }

        // ***
        // Kiem tra Banking account (PAYMENT) duplicated
        // ***
        if(data.getAccountType().equals(AccountTypes.PAYMENT.name().toUpperCase())) {
            if(existedBankingByCifCode(data.getCifCode())) {
                throw new ExistedDataException(MessageKeys.EXISTED_BANKING);
            }
        }

        AccountCommons accountCommons = AccountCommons.builder()
                .customerId(data.getCustomerId())
                .cifCode(data.getCifCode())
                .accountNumber(data.getAccountNumber())
                .accountType(AccountTypes.valueOf(data.getAccountType()))
                .status(AccountStatus.valueOf(data.getStatus()))
                .build();

        return repository.save(accountCommons);
    }

    @Override
//    @CachePut(value = "account_detail", key = "#id")
    public AccountCommons updateStatus(String id, String status) {
        AccountCommons accountCommons = repository.findByAccountCommonIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);

        accountCommons.setStatus(AccountStatus.valueOf(status));

        return repository.save(accountCommons);
    }

    @Override
    public AccountCommons updateStatusByAccountNumber(String accountNumber, String status) {
        AccountCommons accountCommons = repository.findByAccountNumberAndDeleted(accountNumber, false)
                .orElseThrow(ResourceNotFoundException::new);

        accountCommons.setStatus(AccountStatus.valueOf(status));

        return repository.save(accountCommons);
    }

    @Override
//    @CacheEvict(value = "account_detail", key = "#id")
    public void delete(String id) {
        AccountCommons data = repository.findByAccountCommonIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);

        data.setDeleted(true);
        repository.save(data);
    }

    @Override
//    @Cacheable(value = "account_detail", key = "#id", unless = "#result == null")
    public AccountCommons findById(String id) {
        return repository.findByAccountCommonIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public AccountCommons findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumberAndDeleted(accountNumber, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public List<AccountCommons> findAllByCifCode(String cifCode) {
        return repository.findAllByCifCodeAndDeleted(cifCode, false);
    }

    @Override
    public AccountCommons findCommonBankingByCifCode(String cifCode) {
        List<AccountCommons> commons = findAllByCifCode(cifCode);

        return commons.stream()
                .filter(cm -> cm.getAccountType().equals(AccountTypes.PAYMENT))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public PageDataDTO<AccountCommons> findAll(Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page-1, pageSize, Sort.by("createdAt"));
        Page<AccountCommons> pageData = repository.findAllByDeleted(false, pageable);
        List<AccountCommons> listData = pageData.stream().toList();

        return PageDataDTO.<AccountCommons> builder()
                .total(pageData.getTotalElements())
                .listData(listData)
                .build();
    }

    private Boolean existedBankingByCifCode(String cifCode) {
        List<AccountCommons> commons = findAllByCifCode(cifCode);

        long count =
                commons.stream()
                .filter(common -> common.getAccountType().equals(AccountTypes.PAYMENT))
                .count();

        return count > 0;
    }
}
