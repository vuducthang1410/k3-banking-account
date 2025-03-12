package com.system.account_service.services;

import com.system.account_service.dtos.credit.CreateCreditDTO;
import com.system.account_service.dtos.credit.CreditProfileDTO;
import com.system.account_service.dtos.credit.CreditRp;
import com.system.account_service.dtos.response.PageDataDTO;
import com.system.account_service.entities.CreditAccount;
import com.system.common_library.dto.report.AccountReportRequest;
import com.system.common_library.dto.response.customer.CustomerCoreDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;

import java.math.BigDecimal;
import java.util.List;

public interface CreditAccountService {
    CreditRp create(CustomerCoreDTO coreCustomer, CustomerDetailDTO customer, CreateCreditDTO data);

    CreditRp active(String creditId, String customerId, CreditProfileDTO profile);

    void updateDebtBalance(String id, BigDecimal debtBalance);

    CreditAccount updateBalanceByCommonId(String commonId, BigDecimal amount);

    void delete(String id);

    CreditRp findById(String id);

    CreditAccount findByCommonId(String commonId);

    PageDataDTO<CreditRp> findAll(Integer page, Integer pageSize);

    List<CreditAccount> findAllByBranchId(String branchId);

    CreditAccount getDataId(String id);

    List<CreditAccount> getReportsByRange(AccountReportRequest request);
}
