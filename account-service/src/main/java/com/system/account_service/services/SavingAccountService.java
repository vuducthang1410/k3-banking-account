package com.system.account_service.services;

import com.system.account_service.dtos.response.PageDataDTO;
import com.system.account_service.dtos.saving.CreateSavingDTO;
import com.system.account_service.dtos.saving.SavingRp;
import com.system.account_service.entities.SavingAccount;
import com.system.common_library.dto.report.AccountReportRequest;
import com.system.common_library.dto.response.customer.CustomerCoreDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;

import java.math.BigDecimal;
import java.util.List;

public interface SavingAccountService {
    SavingRp create(CustomerCoreDTO coreCustomer, CustomerDetailDTO customer, CreateSavingDTO data);

    void updateBalance(String id, BigDecimal balance);

    SavingAccount updateBalanceByCommonId(String commonId, BigDecimal amount);

    void delete(String id);

    SavingRp findById(String id);

    SavingAccount findByCommonId(String commonId);

    PageDataDTO<SavingRp> findAll(Integer page, Integer pageSize);

    List<SavingAccount> findAllByBranchId(String branchId);

    SavingAccount getDataId(String id);

    List<SavingAccount> getReportsByRange(AccountReportRequest request);
}
