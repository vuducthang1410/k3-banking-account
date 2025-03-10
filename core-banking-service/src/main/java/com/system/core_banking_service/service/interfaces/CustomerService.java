package com.system.core_banking_service.service.interfaces;

import com.system.common_library.dto.request.customer.CreateCustomerCoreDTO;
import com.system.common_library.dto.request.customer.UpdateCustomerCoreDTO;
import com.system.common_library.dto.response.PagedDTO;
import com.system.common_library.dto.response.customer.CustomerCoreDTO;
import com.system.common_library.dto.response.customer.CustomerExtraCoreDTO;

public interface CustomerService {

    CustomerExtraCoreDTO findById(String id);

    CustomerExtraCoreDTO findByCifCode(String cifCode);

    PagedDTO<CustomerCoreDTO> findAllByCondition(Boolean isActive, String search, String sort, int page, int limit);

    CustomerCoreDTO create(CreateCustomerCoreDTO create);

    CustomerCoreDTO update(UpdateCustomerCoreDTO update, String cifCode);

    void delete(String id);
}
