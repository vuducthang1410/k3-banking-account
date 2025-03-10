package com.system.transaction_service.client.napas;

import com.system.common_library.dto.request.transaction.CreateNapasTransactionDTO;
import com.system.common_library.dto.request.transaction.NapasTransactionRollbackDTO;
import com.system.common_library.dto.response.transaction.TransactionCoreNapasDTO;
import com.system.transaction_service.client.napas.fallback.TransactionNapasFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        value = "napas-transaction-client",
        url = "${napas.service.url}/transactions",
        fallback = TransactionNapasFeignClientFallback.class
)
public interface TransactionNapasFeignClient {

    // Create transaction in Napas service
    @PostMapping("")
    ResponseEntity<TransactionCoreNapasDTO> createTransaction(@RequestBody @Validated CreateNapasTransactionDTO create);

    // Rollback transaction in Napas service
    @PostMapping("/rollback")
    ResponseEntity<String> rollbackTransaction(@RequestBody @Validated NapasTransactionRollbackDTO rollback);
}
