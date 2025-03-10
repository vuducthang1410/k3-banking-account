package com.system.account_service.services;

import com.system.account_service.dtos.account_common.CreateAccountCommonDTO;
import com.system.account_service.dtos.response.PageDataDTO;
import com.system.account_service.entities.AccountCommons;

import java.util.List;

public interface AccountCommonService {
    AccountCommons create(CreateAccountCommonDTO data);

    AccountCommons updateStatus(String id, String status);

    AccountCommons updateStatusByAccountNumber(String accountNumber, String status);

    void delete(String id);

    AccountCommons findById(String id);

    AccountCommons findByAccountNumber(String accountNumber);

    List<AccountCommons> findAllByCifCode(String cifCode);

    AccountCommons findCommonBankingByCifCode(String cifCode);

    PageDataDTO<AccountCommons> findAll(Integer page, Integer pageSize);
}
