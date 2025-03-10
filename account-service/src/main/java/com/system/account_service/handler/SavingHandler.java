package com.system.account_service.handler;

import com.system.account_service.client.CoreAccountClient;
import com.system.account_service.client.CoreCustomerClient;
import com.system.account_service.dtos.saving.CreateSavingDTO;
import com.system.account_service.dtos.saving.SavingRp;
import com.system.account_service.exception.payload.InvalidParamException;
import com.system.account_service.services.SavingAccountService;
import com.system.account_service.utils.MessageKeys;
import com.system.common_library.dto.response.customer.CustomerCoreDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.ObjectStatus;
import com.system.common_library.service.CustomerDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SavingHandler {
    private final SavingAccountService service;
    // dubbo service
    @DubboReference
    private final CustomerDubboService customerDubboService;

    // feign client
    private final CoreCustomerClient coreCustomerClient;
    private final CoreAccountClient coreAccountClient;

    public SavingRp create(CreateSavingDTO data) {
        // ***
        // Lay thong tin Customer trong CustomerService = CIF code
        // ***
        CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(data.getCifCode());

        // ***
        // Lay thong tin Customer trong CoreBanking = CIF code
        // ***
        ResponseEntity<CustomerCoreDTO> resCoreCustomer = coreCustomerClient.getByCifCode(data.getCifCode());
        CustomerCoreDTO coreCustomer = resCoreCustomer.getBody();

        // ***
        // Xac thuc account exists & active
        // ***
        if((coreCustomer == null) || (customer == null)) {
            throw new InvalidParamException(MessageKeys.DUBBO_CUSTOMER_INVALID);
        }
        if((!coreCustomer.getIsActive()) || !(customer.getStatus().equals(ObjectStatus.ACTIVE))) {
            throw new InvalidParamException(MessageKeys.DUBBO_CUSTOMER_NOT_ACTIVE);
        }
        // ***
        // Xac thuc Customer hop le (CIF code & Info hop le): So sanh 2 customer trong Service vs Core
        // ***
        if(!(customer.getFullName().equals(coreCustomer.getFullName()))
                || !(customer.getPhone().equals(coreCustomer.getPhone()))
                || !(customer.getMail().equals(coreCustomer.getEmail()))
        ) {
            throw new InvalidParamException(MessageKeys.DUBBO_CUSTOMER_INVALID);
        }

        return service.create(coreCustomer, customer, data);
    }
}
