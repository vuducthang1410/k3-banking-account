package com.system.account_service.client.fallback;

import com.system.account_service.client.CoreCustomerClient;
import com.system.common_library.dto.response.customer.CustomerCoreDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CoreCustomerClientFallback implements CoreCustomerClient {

    @Override
    public ResponseEntity<CustomerCoreDTO> getByCifCode(String cifCode) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}
