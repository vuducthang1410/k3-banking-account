package com.system.customer_service.client.fallback;

import com.system.common_library.dto.request.customer.CreateCustomerCoreDTO;
import com.system.common_library.dto.response.customer.CustomerCoreDTO;
import com.system.common_library.dto.response.customer.CustomerExtraCoreDTO;
import com.system.customer_service.client.CustomerCoreFeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public class CustomerCoreFeignClientFallback implements CustomerCoreFeignClient {

    @Override
    public ResponseEntity<CustomerCoreDTO> create(@RequestBody @Validated CreateCustomerCoreDTO create) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @Override
    public void delete(@PathVariable(value = "id") String id) {
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @Override
    public ResponseEntity<CustomerExtraCoreDTO> getByCifCode(@PathVariable(value = "cif") String cifCode) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}
