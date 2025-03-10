package com.system.transaction_service.client.napas;


import com.system.transaction_service.client.napas.fallback.BankNapasFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        value = "napas-bank-client",
        url = "${napas.service.url}/banks",
        fallback = BankNapasFeignClientFallback.class
)
public interface BankNapasFeignClient {

    // Get bank list in Napas service
    @GetMapping("")
    ResponseEntity<?> getList(@RequestParam(defaultValue = "") String search,
                              @RequestParam(defaultValue = "") Boolean isAvailable,
                              @RequestParam(defaultValue = "id,desc") String sort,
                              @RequestParam(defaultValue = "0") Integer page,
                              @RequestParam(defaultValue = "10") Integer limit);

    // Get bank by id in Napas service
    @GetMapping("/{id}")
    ResponseEntity<?> getById(@PathVariable(value = "id") String id);
}
