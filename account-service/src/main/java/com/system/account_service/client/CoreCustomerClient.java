package com.system.account_service.client;

import com.system.account_service.client.fallback.CoreCustomerClientFallback;
import com.system.common_library.dto.response.customer.CustomerCoreDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        value = "core-customer-client",
        url = "${service.core.banking.url}/customers",
        fallback = CoreCustomerClientFallback.class
)
public interface CoreCustomerClient {

//    Todo: Get Customer info by CIF code in CoreBanking
    @GetMapping("/cif/{cif}")
    ResponseEntity<CustomerCoreDTO> getByCifCode(@PathVariable(value = "cif") String cifCode);
}
