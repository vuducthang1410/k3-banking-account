package com.system.customer_service.client;

import com.system.common_library.dto.request.customer.CreateCustomerCoreDTO;
import com.system.common_library.dto.response.customer.CustomerCoreDTO;
import com.system.common_library.dto.response.customer.CustomerExtraCoreDTO;
import com.system.customer_service.client.fallback.CustomerCoreFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        value = "core-customer-client",
        url = "${core.banking.service.url}/customers",
        fallback = CustomerCoreFeignClientFallback.class
)
public interface CustomerCoreFeignClient {

    @PostMapping("")
    ResponseEntity<CustomerCoreDTO> create(@RequestBody @Validated CreateCustomerCoreDTO create);

    @DeleteMapping("/{id}")
    void delete(@PathVariable(value = "id") String id);

    @GetMapping("/cif/{cif}")
    ResponseEntity<CustomerExtraCoreDTO> getByCifCode(@PathVariable(value = "cif") String cifCode);
}
