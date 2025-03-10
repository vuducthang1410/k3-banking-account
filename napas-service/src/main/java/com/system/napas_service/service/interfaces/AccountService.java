package com.system.napas_service.service.interfaces;

import com.system.common_library.dto.response.account.AccountExtraNapasDTO;
import com.system.common_library.dto.response.account.AccountNapasDTO;
import com.system.napas_service.dto.account.AccountDTO;
import com.system.napas_service.dto.account.CreateAccountDTO;
import com.system.napas_service.dto.account.UpdateAccountDTO;
import com.system.napas_service.dto.response.PagedDTO;

public interface AccountService {

    AccountExtraNapasDTO findByAccountNumber(String accountNumber);

    PagedDTO<AccountNapasDTO> findAllByCondition(Boolean isActive, String search, String sort, int page, int limit);

    AccountDTO create(CreateAccountDTO create);

    AccountDTO update(UpdateAccountDTO update, String accountNumber);

    void delete(String id);
}
