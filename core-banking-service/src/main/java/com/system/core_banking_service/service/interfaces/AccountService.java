package com.system.core_banking_service.service.interfaces;

import com.system.common_library.dto.request.account.CreateAccountCoreDTO;
import com.system.common_library.dto.request.account.UpdateAccountCoreDTO;
import com.system.common_library.dto.response.PagedDTO;
import com.system.common_library.dto.response.account.AccountCoreDTO;
import com.system.common_library.dto.response.account.AccountExtraCoreDTO;
import com.system.common_library.enums.AccountType;

import java.util.List;

public interface AccountService {

    AccountExtraCoreDTO findByAccountNumber(String accountNumber);

    PagedDTO<AccountCoreDTO> findAllByCondition
            (List<AccountType> typeList, Boolean isActive, String search, String sort, int page, int limit);

    AccountCoreDTO create(CreateAccountCoreDTO create);

    AccountCoreDTO update(UpdateAccountCoreDTO update, String accountNumber);

    void delete(String id);
}
